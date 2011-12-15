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

/* $Id: AbstractPackagingMojo.java,v 1.16 2007/08/07 11:29:59 robert Exp $
 *
 * maven-pkg-plugin, Packaging plugin for Maven2 
 * Copyright (C) 2007 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'maven-pkg-plugin'
 * written by Robert Schuster, Fabian Koester. 
 * signature of Elmar Geese, 1 June 2002
 * Elmar Geese, CEO tarent GmbH
 */

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import de.tarent.maven.plugins.pkg.map.PackageMap;

/**
 * Base Mojo for all packaging mojos. It provides convenient access to a mean to
 * resolve the project's complete dependencies.
 */
public abstract class AbstractPackagingMojo extends AbstractMojo {

	private static final String DEFAULT_SRC_AUXFILESDIR = "src/main/auxfiles";

	public static String getDefaultSrcAuxfilesdir() {
		return DEFAULT_SRC_AUXFILESDIR;
	}

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Artifact factory, needed to download source jars.
	 * 
	 * @component role="org.apache.maven.project.MavenProjectBuilder"
	 * @required
	 * @readonly
	 */
	protected MavenProjectBuilder mavenProjectBuilder;

	/**
	 * Temporary directory that contains the files to be assembled.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected File buildDir;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
	 * @required
	 * @readonly
	 */
	protected ArtifactFactory factory;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver resolver;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component 
	 *            role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
	 * @required
	 * @readonly
	 */
	protected ArtifactMetadataSource metadataSource;

	/**
	 * Location of the local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	protected ArtifactRepository local;

	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	protected List<ArtifactRepository> remoteRepos;

	/**
	 * @parameter expression="${project.artifact}"
	 * @required
	 * @readonly
	 */
	protected Artifact artifact;

	/**
	 * @parameter expression="${project.artifactId}"
	 * @required
	 * @readonly
	 */
	protected String artifactId;

	/**
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 * @readonly
	 */
	protected String finalName;

	/**
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected File outputDirectory;

	/**
	 * @parameter expression="${project.version}"
	 * @required
	 * @readonly
	 */
	protected String version;

	/**
	 * JVM binary used to run Java programs from within the Mojo.
	 * 
	 * @parameter expression="${javaExec}" default-value="java"
	 * @required
	 * 
	 */
	protected String javaExec;

	/**
	 * 7Zip binary used to run Java programs from within the Mojo.
	 * 
	 * @parameter expression="${7zipExec}" default-value="7zr"
	 * @required
	 * 
	 */
	protected String _7zipExec;

	/**
	 * Location of the custom package map file. When specifying this one the
	 * internal package map will be overridden completely.
	 * 
	 * @parameter expression="${defPackageMapURL}"
	 */
	protected URL defaultPackageMapURL;

	/**
	 * Location of the auxiliary package map file. When this is specified the
	 * information in the document will be added to the default one.
	 * 
	 * @parameter expression="${auxPackageMapURL}"
	 */
	protected URL auxPackageMapURL;

	/**
	 * Overrides "defaultDistro" parameter. For use on the command-line.
	 * 
	 * @parameter expression="${distro}"
	 */
	protected String distro;

	/**
	 * Overrides "defaultIgnorePackagingTypes" defines a list of comma
	 * speparated packaging types that, when used, will skip copying the main
	 * artifact for the project (if any) in the final package. For use on the
	 * command-line.
	 * 
	 * @parameter expression="${ignorePackagingTypes}" default-value="pom"
	 * @required
	 */
	protected String ignorePackagingTypes;
	
	/**
	 * Parameter with a comma separated list of targets. For use on the command-line.
	 * 
	 * @parameter expression="${target}"
	 */
	protected String target;

	protected PackageMap pm;

	/**
	 * @parameter
	 */
	protected List<TargetConfiguration> targetConfigurations;

	public String get_7zipExec() {
		return _7zipExec;
	}

	public File getBuildDir() {
		return buildDir;
	}

	public ArtifactFactory getFactory() {
		return factory;
	}

	public String getFinalName() {
		return finalName;
	}

	public String getIgnorePackagingTypes() {
		return ignorePackagingTypes;
	}

	public String getJavaExec() {
		return javaExec;
	}

	public ArtifactRepository getLocalRepo() {
		return local;
	}

	public ArtifactMetadataSource getMetadataSource() {
		return metadataSource;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public PackageMap getPm() {
		return pm;
	}

	public MavenProject getProject() {
		return project;
	}

	public List<ArtifactRepository> getRemoteRepos() {
		return remoteRepos;
	}

	public ArtifactResolver getResolver() {
		return resolver;
	}

	public List<TargetConfiguration> getTargetConfigurations() {
		return targetConfigurations;
	}

	public void setPm(PackageMap pm) {
		this.pm = pm;
	}

	protected final boolean packagingTypeBelongsToIgnoreList() {
		boolean inList = false;
		Log l = getLog();
		l.info("ignorePackagingTypes set. Contains: " + ignorePackagingTypes + " . Project packaging is "
				+ project.getPackaging());
		for (String s : ignorePackagingTypes.split(",")) {
			if (project.getPackaging().compareToIgnoreCase(s) == 0) {
				inList = true;
			}
		}
		return inList;
	}
	  
	  /**
	   * 
	   * Returns the comma separated target list as String[].</br></br> 
	   * 
	   * In order to allow multiple target to be called through the command line we allow the user to provide
	   * a comma separated list (Maven < 3.0.3 does not transform comma separated values 
	   * from the command line to String[]). 
	   * 
	   * @param target
	   * @return
	   */
	  protected String[] getTargets()throws MojoExecutionException{
	  
		String[] targetArray = null;
		
		if (target!=null){
			targetArray =  target.split(",");
		}else{
			throw new MojoExecutionException("No target(s) specified for execution.");
		}
		
		return targetArray;
		
	  }
	

	  /**
	   * Validates arguments and test tools.
	   * 
	   * @throws MojoExecutionException
	   */
	  protected void checkEnvironment(Log l, TargetConfiguration tc) throws MojoExecutionException
	  {
	    l.info("distribution             : " + tc.getChosenDistro());
	    l.info("package system           : " + pm.getPackaging());
	    l.info("default package map      : "
	           + (defaultPackageMapURL == null ? "built-in"
	                                          : defaultPackageMapURL.toString()));
	    l.info("auxiliary package map    : "
	           + (auxPackageMapURL == null ? "no" : auxPackageMapURL.toString()));
	    l.info("type of project          : "
	           + ((tc.getMainClass() != null) ? "application" : "library"));
	    l.info("section                  : " + tc.getSection());
	    l.info("bundle all dependencies  : " + ((tc.isBundleAll()) ? "yes" : "no"));
	    l.info("ahead of time compilation: " + ((tc.isAotCompile()) ? "yes" : "no"));
	    l.info("custom jar libraries     : "
	            + ((tc.jarFiles.isEmpty()) ? "<none>"
	                                      : String.valueOf(tc.jarFiles.size())));
	    l.info("JNI libraries            : "
	           + ((tc.jniFiles.isEmpty()) ? "<none>"
	                                     : String.valueOf(tc.jniFiles.size())));
	    l.info("auxiliary file source dir: "
	           + (tc.srcAuxFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                             : tc.srcAuxFilesDir));
	    l.info("auxiliary files          : "
	           + ((tc.auxFiles.isEmpty()) ? "<none>"
	                                     : String.valueOf(tc.auxFiles.size())));
	    l.info("prefix                   : "
	           + (tc.prefix.length() == 1 ? "/ (default)" : tc.prefix));
	    l.info("sysconf files source dir : "
	           + (tc.srcSysconfFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                                 : tc.srcSysconfFilesDir));
	    l.info("sysconfdir               : "
	           + (tc.sysconfdir.length() == 0 ? "(default)" : tc.sysconfdir));
	    l.info("dataroot files source dir: "
	           + (tc.srcDatarootFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                                  : tc.srcDatarootFilesDir));
	    l.info("dataroot                 : "
	           + (tc.datarootdir.length() == 0 ? "(default)" : tc.datarootdir));
	    l.info("data files source dir    : "
	           + (tc.srcDataFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                              : tc.srcDataFilesDir));
	    l.info("datadir                  : "
	           + (tc.datadir.length() == 0 ? "(default)" : tc.datadir));
	    l.info("bindir                   : "
	           + (tc.bindir.length() == 0 ? "(default)" : tc.bindir));

	    if (tc.getChosenDistro() == null)
	      throw new MojoExecutionException("No distribution configured!");

	    if (tc.isAotCompile())
	      {
	        l.info("aot compiler             : " + tc.getGcjExec());
	        l.info("aot classmap generator   : " + tc.getGcjDbToolExec());
	      }

	    if (tc.getMainClass() == null)
	      {
	        if (! "libs".equals(tc.getSection()))
	          throw new MojoExecutionException(
	                                           "section has to be 'libs' if no main class is given.");

	        if (tc.isBundleAll())
	          throw new MojoExecutionException(
	                                           "Bundling dependencies to a library makes no sense.");
	      }
	    else
	      {
	        if ("libs".equals(tc.getSection()))
	          throw new MojoExecutionException(
	                                           "Set a proper section if main class parameter is set.");
	      }

	    if (tc.isAotCompile())
	      {
	        AotCompileUtils.setGcjExecutable(tc.getGcjExec());
	        AotCompileUtils.setGcjDbToolExecutable(tc.getGcjDbToolExec());

	        AotCompileUtils.checkToolAvailability();
	      }
	  }

}
