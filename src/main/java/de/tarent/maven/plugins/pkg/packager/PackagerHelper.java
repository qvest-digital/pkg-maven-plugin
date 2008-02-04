package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;

import de.tarent.maven.plugins.pkg.Packaging;

public interface PackagerHelper
{
  String getPackageVersion();

  String getPackageName();

  String getArtifactId();

  String getProjectDescription();
  
  String getProjectUrl();

  String getAotPackageName();
  
  File getBasePkgDir();
  
  File getSourceArtifactFile();
  
  File getDestArtifactFile();
  
  File getDefaultDestBundledArtifactsDir();
  
  File getWrapperScriptFile(File base);
  
  File getTempRoot();
  
  File getOutputDirectory();
  
  File getAotPkgDir();
  
  void prepareInitialDirectories() throws MojoExecutionException;

  void prepareAotDirectories() throws MojoExecutionException;
  
  void copyArtifact() throws MojoExecutionException;
  
  void copyJNILibraries() throws MojoExecutionException;
  
  void copyResources() throws MojoExecutionException;
  
  Set copyDependencies(File libraryRoot, File artifactFile) throws MojoExecutionException;
  
  void generateWrapperScript(Set bundledArtifacts, String bootclasspath, String classpath) throws MojoExecutionException;
  
  long copyArtifacts(Set artifacts, File dst) throws MojoExecutionException;
  
  String createDependencyLine() throws MojoExecutionException;
  
  void createClasspathLine(Set bundledArtifacts, StringBuilder bcp, StringBuilder cp) throws MojoExecutionException;

  File getIzPackSrcDir();
  
  String getJavaExec();

  File getDefaultAuxFileSrcDir();
}
