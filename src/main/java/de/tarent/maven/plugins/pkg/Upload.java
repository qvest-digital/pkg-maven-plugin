package de.tarent.maven.plugins.pkg;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.upload.APTUploader;
import de.tarent.maven.plugins.pkg.upload.IPkgUploader;
import de.tarent.maven.plugins.pkg.upload.WagonUploader;

/**
 * Enables the plugin to transfer packages resulting from a TargetConfiguration 
 * to external repositories and/or local directories.</br>
 * This goal uses codehaus' wagon-maven-plugin behind the scenes.</br>
 * Tested providers are: ssh (scpexe://), sftp (sftp://), file (file://)</br>
 * 
 * @phase "deploy"
 * @goal upload
 */
public class Upload extends AbstractPackagingMojo {

	Log l = getLog();
	
	@Override
	protected void executeTargetConfiguration(WorkspaceSession ws, String distro)
			throws MojoExecutionException, MojoFailureException {
		TargetConfiguration tc = ws.getTargetConfiguration();		
		UploadParameters param;
		
		try{
			param = tc.getUploadParameters();
		}catch (Exception ex){
			throw new MojoExecutionException("No upload paramenters found for configuration " + tc.getTarget(), ex);			
		}	
		
			File packageFile = getPackageFile(ws);

			l.info("Name of package is: " + packageFile.getAbsolutePath());
			if(packageFile.exists()){
				l.info("Package file exists");
			}else{
				throw new MojoExecutionException("Package file does not exist.");
			}
			if (param != null) {
				for (String url : param.getUrls()) {
					l.info("Starting upload routine to " + url);
					IPkgUploader iup;
					iup = getUploaderForProtocol(ws,param.parseUrlPlaceholders(url));
					iup.uploadPackage();
				}
			} else {
				throw new MojoExecutionException("No upload url(s) set for " + tc.getTarget());
			}		
	}

	private IPkgUploader getUploaderForProtocol(WorkspaceSession ws, String url) {

		if(url.startsWith("debapt://")){
			return new APTUploader(ws, url.replace("debapt://",""));
		}else{
			return new WagonUploader(ws, url);
		}

	}
	
	public File getPackageFile(WorkspaceSession ws) {
		return new File(ws.getMojo().getBuildDir(), ws.getHelper().getPackageFileName());
	}

}
