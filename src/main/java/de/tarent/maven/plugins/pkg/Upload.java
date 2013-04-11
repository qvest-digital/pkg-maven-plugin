package de.tarent.maven.plugins.pkg;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.upload.APTUploader;
import de.tarent.maven.plugins.pkg.upload.IPkgUploader;
import de.tarent.maven.plugins.pkg.upload.RepreproDeployer;
import de.tarent.maven.plugins.pkg.upload.WagonUploader;

/**
 * Enables the plugin to transfer packages resulting from a TargetConfiguration
 * to external repositories and/or local directories.</br> This goal uses
 * codehaus' wagon-maven-plugin behind the scenes.</br> Tested providers are:
 * ssh (scpexe://), sftp (sftp://), file (file://)</br>
 * 
 * @phase deploy
 * @goal upload
 * @requiresProject
 */
public class Upload extends AbstractPackagingMojo {

	private static final String duploadURIScheme = "dupload://";
	private static final String repreproURIScheme = "reprepro://";
	Log l = getLog();

	@Override
	protected void executeTargetConfiguration(WorkspaceSession ws)
			throws MojoExecutionException, MojoFailureException {
		TargetConfiguration tc = ws.getTargetConfiguration();
		UploadParameters param;

		try {
			param = tc.getUploadParameters();
		} catch (Exception ex) {
			throw new MojoExecutionException(
					"No upload paramenters found for configuration "
							+ tc.getTarget(), ex);
		}

		File packageFile = getPackageFile(ws);

		l.info("Name of package is: " + packageFile.getAbsolutePath());
		if (packageFile.exists()) {
			l.info("Package file exists");
		} else {
			throw new MojoExecutionException("Package file does not exist.");
		}
		if (param != null) {
			for (String url : param.getUrls()) {
				l.info("Starting upload routine to " + url);
				IPkgUploader iup;
				iup = getUploaderForProtocol(ws,
						param.parseUrlPlaceholders(url));
				iup.uploadPackage();
			}
		} else {
			throw new MojoExecutionException("No upload url(s) set for "
					+ tc.getTarget());
		}
	}

	/**
	 * <p>
	 * Retunrs the appropiate uploader depending on the protocol used by a url.
	 * </p>
	 * At the moment this method only differentiantes between dupload:// (these
	 * urls will be managed by our own class, which uses dupload in the
	 * background) and everything else (managed by codehaus'
	 * wagon-maven-plugin).
	 * 
	 * @param ws
	 * @param url
	 * @return
	 */
	private IPkgUploader getUploaderForProtocol(WorkspaceSession ws, String url) {

		if (url.startsWith(duploadURIScheme)) {
			return new APTUploader(ws, url.replace(duploadURIScheme, ""));
		} else if (url.startsWith(repreproURIScheme)) {
			return new RepreproDeployer(ws, url.replace(repreproURIScheme, ""));
		} else {
			return new WagonUploader(ws, url);
		}
	}

	/**
	 * Returns the filename of the package to be build for a specific
	 * WorkspaceSession
	 * 
	 * @param ws
	 * @return
	 */
	public File getPackageFile(WorkspaceSession ws) {
		return new File(ws.getMojo().getBuildDir(), ws.getHelper()
				.getPackageFileName());
	}

}
