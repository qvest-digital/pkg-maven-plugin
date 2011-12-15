package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.logging.Log;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironmentM2;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;

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
	
	/**
	 * The Maven Session Object
	 * 
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	protected MavenSession session;
	/**
	 * The Maven PluginManager Object
	 * 
	 * @component
	 * @required
	 */
	protected PluginManager pluginManager;

	@Override
	public void execute() throws MojoExecutionException {
		for (String currentTarget : getTargets()) {
			initiateUpload(currentTarget);
		}
	}

	private void initiateUpload(String targetString) throws MojoExecutionException {
		UploadParameters param;
		TargetConfiguration currentTarget = Utils.getTargetConfigurationFromString(targetString, targetConfigurations);
		
		try{
			param = currentTarget.getUploadParameters();
		}catch (Exception ex){
			throw new MojoExecutionException("No upload paramenters found for configuration " + targetString, ex);			
		}			
			String distro = Utils.getDefaultDistro(targetString,targetConfigurations,l);
			PackageMap packageMap = new PackageMap(defaultPackageMapURL, defaultPackageMapURL, distro, null);
			File packageFile = getPackageFile(currentTarget, packageMap, targetString);

			l.info("Name of package is: " + packageFile.getAbsolutePath());
			if(packageFile.exists()){
				l.info("Package file exists");
			}else{
				throw new MojoExecutionException("Package file does not exist.");
			}
			if (param != null) {
				for (String url : param.getUrls()) {
					l.info("Starting upload routine to " + url);
					try{
						MojoExecutor.executeMojoImpl(MojoExecutor.plugin("org.codehaus.mojo", "wagon-maven-plugin"),
								MojoExecutor.goal("upload-single"),
								MojoExecutor.configuration(generateUploadElements(packageFile, url, param)),
								new ExecutionEnvironmentM2(project, session, pluginManager));
						l.info("Upload successful to " + url);
					}catch(Exception ex){
						throw new MojoExecutionException("Error while uploading file: " +ex.getMessage(),ex);
					}
					
				}
			} else {
				throw new MojoExecutionException("No upload url(s) set for " + targetString);
			}		
	}

	public File getPackageFile(TargetConfiguration currentTargetConfiguration, PackageMap packageMap,
			String targetString) {

		currentTargetConfiguration.setSection("misc");
		Helper helper = Utils.getPackagingHelperForPackaging(this, packageMap, currentTargetConfiguration);

		return new File(helper.getTempRoot().getParent(), helper.generatePackageFileName());
	}

	/**
	 * Generates a an Array of Elements containing the needed information for an upload
	 * 
	 * @param file
	 * @param url
	 * @param param
	 * @return
	 * @throws MojoExecutionException
	 */
	public Element[] generateUploadElements(File file, String url, UploadParameters param) throws MojoExecutionException {
		
		List<Element> elements = new ArrayList<Element>();
		elements.add(new Element("fromFile", file.getAbsolutePath()));
		
		if(param.getToDir()!=null)
			elements.add(new Element("toDir", param.getToDir()));
		
		if(param.getServerId()!=null)
			elements.add(new Element("serverId", param.getToDir()));
		
		elements.add(new Element("url", param.parseUrlPlaceholders(url)));
		
		Element[] e = new Element[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			e[i] = elements.get(i);
		}
		return e;

	}

}
