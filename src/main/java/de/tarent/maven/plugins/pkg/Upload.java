package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;
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
	    AbstractPackagingMojo mojo = ws.getMojo();
	    TargetConfiguration tc = ws.getTargetConfiguration();
	    Map<String, TargetConfiguration> tcMap = ws.getTargetConfigurationMap();
	    
	    // At first we create the various work objects that we need to process the
	    // request to package what is specified in 'tc' and test their validity.

	    // Resolve all the relations of the given target configuration. This can fail
	    // with an exception if there is a configuration mistake.
	    List<TargetConfiguration> resolvedRelations = Utils.resolveConfigurations(
	    		tc.getRelations(), tcMap);
		
	    // Retrieve package map for chosen distro.
	    PackageMap pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, distro,
	                        tc.getBundleDependencies());

	    String packaging = pm.getPackaging();
	    
	    if (packaging == null){
	      throw new MojoExecutionException(
	                                       "Package maps document set no packaging for distro: "
	                                           + tc.getChosenDistro());
	    }
	
	    // Create packager and packaging helper according to the chosen packaging type.
	    // Note: Helper is historically strongly dependent on the mojo, the package and
	    // the targetconfiguration because this makes method calls to the helper so neatly
	    // short and uniform among all Packager implementations, ie. there're almost no
	    // arguments needed and all packagers call the same stuff while in reality they're
	    // subtle differences between them.
	    Helper ph = new Helper();
	    ph.init(mojo, pm, tc, resolvedRelations);

		TargetConfiguration currentTarget = tc;
		UploadParameters param;
		
		try{
			param = currentTarget.getUploadParameters();
		}catch (Exception ex){
			throw new MojoExecutionException("No upload paramenters found for configuration " + tc.getTarget(), ex);			
		}			

			File packageFile = getPackageFile(currentTarget, ph, tc.getTarget());

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
					iup = getUploaderForProtocol(ws,url);
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
	
	public File getPackageFile(TargetConfiguration currentTargetConfiguration, Helper helper,
			String targetString) {
		return new File(helper.getTempRoot().getParent(), helper.getPackageFileName());
	}

}
