/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb, ipk, izpack)
 * Copyright (C) 2000-2008 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 11 March 2008
 * Elmar Geese, CEO tarent GmbH.
 */

/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb and izpack)
 * Copyright (C) 2000-2007 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 14 June 2007
 * Elmar Geese, CEO tarent GmbH.
 */

package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
import de.tarent.maven.plugins.pkg.Path;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.WorkspaceSession;
import de.tarent.maven.plugins.pkg.generator.SpecFileGenerator;
import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.helper.RpmHelper;

/**
 * Creates a RPM package file
 * 
 * @author plafue
 */
public class RPMPackager extends Packager {

	@Override
	public void execute(Log l, WorkspaceSession workspaceSession)
			throws MojoExecutionException {
		TargetConfiguration distroConfig = workspaceSession.getTargetConfiguration();
		RpmHelper ph = (RpmHelper) workspaceSession.getHelper();
		
		ph.prepareInitialDirectories();
		
		//Setting all destination directories to /BUILD/ + target name
		ph.setDstBinDir(new File(ph.getBaseBuildDir(),ph.getTargetBinDir().toString()));
	    ph.setDstSysconfDir(new File(ph.getBaseBuildDir(),ph.getTargetSysconfDir().toString()));
	    ph.setDstDatarootDir(new File(ph.getBaseBuildDir(),ph.getTargetDatarootDir().toString()));
	    ph.setDstDataDir(new File(ph.getBaseBuildDir(),ph.getTargetDataDir().toString()));
	    ph.setDstJNIDir(new File(ph.getBaseBuildDir(),ph.getTargetJNIDir().toString()));	    
	    ph.setDstBundledJarDir(new File(ph.getBaseBuildDir(),ph.getTargetBundledJarDir().toString()));
	    ph.setDstStarterDir(new File(ph.getBaseBuildDir(),ph.getTargetStarterDir().toString()));
	    ph.setDstWrapperScriptFile(new File(ph.getBaseBuildDir(),ph.getTargetWrapperScriptFile().toString()));
	    
	    ph.copyProjectArtifact();	    
		ph.copyFiles();
		
		l.debug(ph.getPackageName());
		l.debug(ph.getPackageVersion());
		l.debug(ph.getBasePkgDir().getPath());

	    /* If there is a main class, we will create a starter script for it
	     * and we will make sure that the bundled artifacts are copied.
	    */
	    if (distroConfig.getMainClass() != null){
		    Path bcp = new Path();
		    Path cp = new Path();
	    	Set<Artifact> bundledArtifacts = ph.createClasspathLine(bcp, cp);
            ph.generateWrapperScript(bundledArtifacts, bcp, cp, false);
            ph.copyArtifacts(bundledArtifacts);
        }

		File specFile = new File(ph.getBaseSpecsDir(), ph.getPackageName() + ".spec");

		try {
	        
	        
			generateSPECFile(l, (RpmHelper) ph, distroConfig, specFile);
			l.info("SPEC file generated.");
			createPackage(l, workspaceSession, specFile);
			l.info("Package created.");
			File resultingPackage = copyRPMToTargetFolder(l, ph, distroConfig);
			l.info("Output of rpm -pqi :");
			String out = IOUtils.toString(Utils.exec(new String[] {"rpm", "-pqi", resultingPackage.getAbsolutePath()},
													 resultingPackage.getParent(),
													 "RPM not found",
													 "RPM not found"));

			l.info("=======================================");
			for(String s: out.split("\\r?\\n")){l.info(s);}
			l.info("=======================================");
			
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.toString());
		} finally {
			try {
				ph.restoreRpmMacrosFileBackup(l);
			} catch (IOException e) {
				throw new MojoExecutionException(e.toString());
			}
		}
	}

	/**
	 * Copies the created artifact from 
	 * @param l 
	 * @param ph
	 * @param distroConfig
	 * @return
	 * @throws IOException
	 */
	private File copyRPMToTargetFolder(Log l, RpmHelper ph, TargetConfiguration distroConfig) throws IOException {
		
		StringBuilder rpmPackagePath= new StringBuilder(ph.getBaseBuildDir().getParent().toString());				
		rpmPackagePath.append("/RPMS/");
		rpmPackagePath.append(distroConfig.getArchitecture());
		rpmPackagePath.append("/");		
		String rpmPackageName = ph.generatePackageFileName();
		
		File targetFile = new File(ph.getTempRoot().getParentFile(),rpmPackageName);
		
		l.debug("Attempting to copy from "+ rpmPackagePath.toString() + rpmPackageName.toString()+
				" to " + ph.getTempRoot().getParent()+"/"+rpmPackageName.toString());
		
		FileUtils.copyFile(new File(rpmPackagePath.toString(),rpmPackageName.toString()), targetFile);
		
		l.info("RPM file copied to "+ph.getTempRoot().getParent()+"/"+rpmPackageName.toString());		
		return targetFile;
	}


	/**
	 * Will prepare the custom Build Area by creating the .rpmmacrosfile
	 * and will check for rpmbuild to exist.
	 */
	@Override
	public void checkEnvironment(Log l, WorkspaceSession workspaceSession) throws MojoExecutionException {
		RpmHelper ph = (RpmHelper) workspaceSession.getHelper();
		try {
			Utils.checkProgramAvailability("rpmbuild");
			l.info(IOUtils.toString(Utils.exec(new String[] {"rpm", "--version"},
					null,"Calling rpm --version failed","ioError",null)).trim());
			ph.createRpmMacrosFile();
			
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

	/**
	 * Takes the parameters inside Packaging.Helper and generates the
	 * spec file needed for rpmbuild to work.
	 * 
	 * @param l
	 * @param ph
	 * @param dc
	 * @param specFile
	 * @throws MojoExecutionException
	 * @throws IOException 
	 */
	private void generateSPECFile(Log l, RpmHelper ph, TargetConfiguration dc,
			File specFile) throws MojoExecutionException, IOException {
		l.info("Creating SPEC file: " + specFile.getAbsolutePath());
		SpecFileGenerator sgen = new SpecFileGenerator();
		sgen.setLogger(l);
		try {				
			sgen.setLogger(l);
			sgen.setBuildroot("%{_builddir}");		
			//sgen.setCleancommands(generateCleanCommands(ph, dc));

			// Following parameters MUST be provided for rpmbuild to work:
			l.info("Adding mandatory parameters to SPEC file.");
			sgen.setPackageName(ph.getPackageName());
			sgen.setVersion(ph.getVersion());
			sgen.setSummary(ph.getProjectDescription());
			sgen.setDescription(ph.getProjectDescription());
			sgen.setLicense(ph.getLicense());
			sgen.setRelease(dc.getRelease());
			sgen.setSource(dc.getSource());
			sgen.setUrl(ph.getProjectUrl());
			sgen.setGroup(dc.getSection());
			sgen.setDependencies(ph.createDependencyLine());
			
			// Following parameters are not mandatory
			l.info("Adding optional parameters to SPEC file.");
			sgen.setArch(dc.getArchitecture());
			sgen.setPrefix(dc.getPrefix());
			sgen.setPackager(dc.getMaintainer());
			sgen.setFiles(ph.generateFilelist());

			sgen.setPreinstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPreinstScript());	
			sgen.setPostinstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPostinstScript());	
			sgen.setPreuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPrermScript());
			sgen.setPostuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPostrmScript());
	
			l.info("Creating SPEC file: " + specFile.getAbsolutePath());
			Utils.createFile(specFile, "spec");
			sgen.generate(specFile);
		} catch (IOException ioe) {
			throw new MojoExecutionException(
					"IOException while creating SPEC file.", ioe);
		} catch (MojoExecutionException e) {
			throw e;
		} catch(NullPointerException e){
			throw new MojoExecutionException(
					"Parameter not found while creating SPEC file.", e);
			
		}

	}

	/**
	 * Executes rpmbuild, that will generate the final rpm package.
	 * 
	 * If the parameter "sign" is set as true in pom, package will 
	 * be signed with the Maintainer (Packager) name provided.
	 * 
	 * @param l
	 * @param ph
	 * @param specFile
	 * @throws MojoExecutionException
	 */
	private void createPackage(Log l, WorkspaceSession workspaceSession, File specFile) 
			throws MojoExecutionException {
		
		RpmHelper ph = (RpmHelper) workspaceSession.getHelper();
		TargetConfiguration dc = workspaceSession.getTargetConfiguration();
		AbstractPackagingMojo apm = workspaceSession.getMojo();
		l.info("Calling rpmbuild to create binary package");
		l.info("Builddir is "+ph.getBaseBuildDir().toString());
		String[] command;		
		if (dc.isSign()) {
			command = new String[] { "rpmbuild", "-bb", "--sign",					
						"--buildroot", ph.getBaseBuildDir().toString(),
						specFile.toString() };

		} else {
			command = new String[] { "rpmbuild", "-bb", "--buildroot",
					ph.getBaseBuildDir().toString(),
					specFile.toString() };
		}

		try{
			if(apm.getSignPassPhrase()!=null && dc.isSign()){
				Utils.exec(command, "'rpmbuild -bb' failed.",
					"Error creating rpm file.",apm.getSignPassPhrase());
			}else{
				Utils.exec(command, "'rpmbuild -bb' failed.",
						"Error creating rpm file.");				
			}
		}catch(MojoExecutionException ex){
			throw ex;
		}
	}
}
