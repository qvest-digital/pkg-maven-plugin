package de.tarent.maven.plugins.pkg.upload;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.WorkspaceSession;

public class RepreproDeployer implements IPkgUploader {
	
	/**
	 * Defines the repository location to use for reprepro. For use on the command-line. 
	 * 
	 */
	protected String repo;	
	protected String packagingType;
	protected Log l;
	protected final File base;
	protected final String packageFileName;
	protected final String distro;	
	protected final static String REPREPRO = "reprepro";
	private File changesFile;
	
	public RepreproDeployer(WorkspaceSession ws, String repo){
		this.l = ws.getMojo().getLog();		
		this.packagingType= ws.getPackageMap().getPackaging();
		this.repo = repo;
		this.base = ws.getMojo().getBuildDir();
		this.packageFileName = ws.getHelper().getPackageFileNameWithoutExtension();
		this.distro = ws.getHelper().getChosenDistro();
		this.changesFile = new File(base,packageFileName + ".changes");
	}

	/**
 	 * Deploys the .deb to a local apt repository. Currently only reprepro is supported.
 	 * @throws MojoExecutionException
 	 */
	@Override
	public void uploadPackage() throws MojoExecutionException {
		
		logParameters();
		checkPrerequisites();
		
		l.info("Deploying " + base.getAbsolutePath() + "/" + packageFileName +".deb");
		
		Utils.exec(generateCommand(), base.getAbsoluteFile(), 
					"Deploying .deb to \"" + repo + "\" failed", 
					"Deploying .deb to \"" + repo + "\" failed");
		
		l.info("Deploying package " +packageFileName + "sucessful.");		

	}
	/**
	 * Generates the command to call
	 * @return
	 */
	protected String[] generateCommand() {
		return new String[] {REPREPRO,"-b" + repo, "include", distro, changesFile.getAbsolutePath()};
	}

	/**
	 * Checks for deb and changes files, as well as the repository location
	 * @throws MojoExecutionException
	 */
	public void checkPrerequisites() throws MojoExecutionException{
		
		if (repo.isEmpty()){
			throw new MojoExecutionException("Repository location can not be empty. Aborting.");
		}
		
		if (!packagingType.equals("deb")){
			throw new MojoExecutionException("Uploading packaging-type '"+ packagingType + "' to reprepro not supported.");
		}
		
		if (!changesFile.exists()){
			throw new MojoExecutionException("Changes file for '"+ packageFileName + "' does not exist.");
		}
	}
	/**
	 * Logs all parameters needed for correct execution of reprepro
	 */
 	private void logParameters() {
		l.info("PackagingType: " + packagingType);
		l.info("Repository location: " + repo);
		l.info("Base: " + base.getAbsolutePath());
		l.info("Package filename: " + packageFileName);
		l.info("distro: " + distro);
		l.info("Changes file: "+changesFile.getAbsolutePath());		
	}
}
