package de.tarent.maven.plugins.pkg.upload;

import org.apache.maven.plugin.MojoExecutionException;

public interface IPkgUploader {

	public void uploadPackage() throws MojoExecutionException;
}
