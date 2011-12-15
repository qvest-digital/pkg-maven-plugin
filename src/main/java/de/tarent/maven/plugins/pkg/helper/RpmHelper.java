package de.tarent.maven.plugins.pkg.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.AuxFile;
import de.tarent.maven.plugins.pkg.Utils;


public class RpmHelper extends Helper {

		/**
	  	 * Convenience field that denotes the BUILD directory
	  	 */
		private File baseBuildDir;
		/**
	  	 * Convenience field that denotes the SPECS directory
	  	 */
		private File baseSpecsDir;

	    public String getVersion()
	    {
	    	return super.getPackageVersion();
	    }

		public File getBaseBuildDir() {
			return baseBuildDir;
		}

		public void setBaseBuildDir(File baseBuildDir) {
			this.baseBuildDir = baseBuildDir;
		}

		public File getBaseSpecsDir() {
			return baseSpecsDir;
		}

		public void setBaseSpecsDir(File baseSpecsDir) {
			this.baseSpecsDir = baseSpecsDir;
		}

		/**
		 * Provides the same functionality as getDstArtifactFile 
		 * in the superclass, but using getBaseBuildDir instead of getBasePkgDir 
		 */
		@Override
	    public File getDstArtifactFile()
	    {
	      if (dstArtifactFile == null)
	        dstArtifactFile = new File(getBaseBuildDir(),
	                                   getTargetArtifactFile().toString());

	      return dstArtifactFile;
	    }
	    
		/**
		 * Provides the same functionality as prepareInitialDirectories 
		 * in the superclass, and extends it setting up directories for RPM compliance   
		 */
		@Override
		public void prepareInitialDirectories() throws MojoExecutionException{
			super.prepareInitialDirectories();
			setBaseBuildDir(new File(basePkgDir,"/BUILD"));
			setBaseSpecsDir(new File(basePkgDir,"/SPECS"));
			// Creating folder structure for RPM creation. Older versions of rpmbuild
			// do not automatically create the directories as needed
			try {
				FileUtils.forceMkdir(new File(basePkgDir,"/RPMS"));
				FileUtils.forceMkdir(new File(basePkgDir,"/RPMS/x86_64"));
				FileUtils.forceMkdir(new File(basePkgDir,"/RPMS/i386"));
				
			} catch (IOException e) {
				throw new MojoExecutionException("Error creating needed folder structure",e);
			}
			
		}
		
		/**
		 * Creates a .rpmmacros file in the users home directory in order to be able
		 * to specify a Build Area other than the user's home.
		 * 
		 * If a .rpmmacros file already exists will be backed up. This file can be
		 * restored using {@link #restorerpmmacrosfile}.
		 * 
		 * @param l
		 * @param basedirectory
		 * @throws IOException
		 * @throws MojoExecutionException 
		 */
		public void createRpmMacrosFile() 
				throws IOException, MojoExecutionException {
			String userHome = System.getProperty("user.home");
			File original = new File(userHome + "/.rpmmacros");
			
			if (original.exists()) {
				if(l!=null){
					l.info("File " + userHome + "/.rpmmacros found. Creating back-up.");
				}
				File backup = new File(userHome + "/.rpmmacros_bck");
				FileUtils.copyFile(original, backup);
			}
			original.delete();
			if (!original.exists()) {
				if(l!=null){
					l.info("Creating " + userHome + "/.rpmmacros file.");
				}
				PrintWriter p = new PrintWriter(original);
				p.print("%_topdir       ");
				p.println(getBasePkgDir().getAbsolutePath());
				p.print("%tmppath       ");
				p.println(getBasePkgDir().getAbsolutePath());

				if (targetConfiguration.getMaintainer() != null) {
					if(l!=null){
						l.info("Maintainer found, its name could be used to sign the RPM.");
					}
					p.print("%_gpg_name       ");
					p.println(targetConfiguration.getMaintainer());
				}

				p.close();
			}
		}

		/**
		 * 
		 * Removes the new macros file and 
		 * restores the backup created by
		 * {@link #createrpmmacrosfile}
		 * 
		 * @param l
		 * @throws IOException
		 */
		public void restoreRpmMacrosFileBackup(Log l) throws IOException {
			String userHome = System.getProperty("user.home");
			File original = new File(userHome + "/.rpmmacros");
			File backup = new File(userHome + "/.rpmmacros_bck");
			if (backup.exists()) {
				if(l!=null){
					l.info("Restoring .rpmmacros backup file.");
				}
				if (original.delete()) {
					FileUtils.copyFile(backup, original);
				}
			} else {
				original.delete();
			}

		}

		public String generatePackageFileName() {
			StringBuilder rpmPackageName = new StringBuilder();			
			rpmPackageName.append(getPackageName());
			rpmPackageName.append("-");
			rpmPackageName.append(getVersion().replace("-", "_"));
			rpmPackageName.append("-");
			rpmPackageName.append(targetConfiguration.getRelease());
			rpmPackageName.append(".");
			rpmPackageName.append(targetConfiguration.getArchitecture());
			rpmPackageName.append(".rpm");
			return rpmPackageName.toString();
		}
		
		
		public List<AuxFile> generateFilelist() throws MojoExecutionException
	    {
			List<AuxFile> list = new ArrayList<AuxFile>();
			
			for (File file : FileUtils.listFiles(getBaseBuildDir(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
			{
		    	  list.add(new AuxFile(file.getAbsolutePath().replace(getBaseBuildDir().toString(),"")));
		    }	  
			return list;
	    }

		/**
		 * Returns a comma separated list of License names based on the licenses provided in the POM
		 * @return
		 * @throws MojoExecutionException 
		 */
		public String getLicense() throws MojoExecutionException {
			
			return Utils.getConsolidatedLicenseString(apm.getProject());
		}
	}
