package de.tarent.maven.plugins.pkg.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironmentM2;

import de.tarent.maven.plugins.pkg.WorkspaceSession;

public class WagonUploader implements IPkgUploader{

	private Log l;
	private MavenProject project;
	private PluginManager pluginManager;
	private MavenSession session;
	private String url;
	private File packageFile;
	
	public WagonUploader(WorkspaceSession ws, String url) {
		l = ws.getMojo().getLog();
		project = ws.getMojo().getProject();
		session = ws.getMojo().getSession();
		pluginManager = ws.getMojo().getPluginManager();
		this.url = url;
		packageFile = new File(ws.getHelper().getTempRoot(), ws.getHelper().getPackageFileName());
		
	}

	@Override
	public void uploadPackage() throws MojoExecutionException  {		
		try{
			MojoExecutor.executeMojoImpl(MojoExecutor.plugin("org.codehaus.mojo", "wagon-maven-plugin"),
					MojoExecutor.goal("upload-single"),
					MojoExecutor.configuration(generateUploadElements(packageFile, url)),
					new ExecutionEnvironmentM2(project, session, pluginManager));
			l.info("Upload successful to " + url);
		}catch(Exception ex){
			throw new MojoExecutionException("Error while uploading file: " +ex.getMessage(),ex);
		}
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
	private Element[] generateUploadElements(File file, String url) throws MojoExecutionException {
		
		List<Element> elements = new ArrayList<Element>();
		elements.add(new Element("fromFile", file.getAbsolutePath()));
		elements.add(new Element("url", url));
		
		Element[] e = new Element[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			e[i] = elements.get(i);
		}
		return e;

	}

}
