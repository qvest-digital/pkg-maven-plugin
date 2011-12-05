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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * Base Mojo for all packaging mojos. It provides convenient access to a mean to
 * resolve the project's complete dependencies.
 */
public abstract class AbstractPackagingMojo extends AbstractMojo
{

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * Artifact factory, needed to download source jars.
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
   * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
   * @required
   * @readonly
   */
  protected ArtifactFactory factory;

  /**
   * Used to look up Artifacts in the remote repository.
   * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
   * @required
   * @readonly
   */
  protected ArtifactResolver resolver;

  /**
   * Used to look up Artifacts in the remote repository.
   * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
   * @required
   * @readonly
   */
  protected ArtifactMetadataSource metadataSource;

  /**
   * Location of the local repository.
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository local;

  /**
   * List of Remote Repositories used by the resolver
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepos;

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
   * Location of the custom package map file. When specifying this one
   * the internal package map will be overridden completely. 
   * 
   * @parameter expression="${defPackageMapURL}"
   */
  protected URL defaultPackageMapURL;

  /**
   * Location of the auxiliary package map file. When this is specified
   * the information in the document will be added to the default one.
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
   * Overrides "defaultIgnorePackagingTypes" defines a list of comma speparated packaging types that, when used, 
   * will skip copying the main artifact for the project (if any) in the final package. For use on the command-line. 
   * 
   * @parameter expression="${ignorePackagingTypes}" default-value="pom"
   * @required
   */
  protected String ignorePackagingTypes; 
  
  /**
   * Set default target configuration to package for.
   * 
   * @parameter expression="${defaultTarget}"
   * @required
   */
  protected String defaultTarget;

  /**
   * Overrides "defaultTarget" parameter with a comma separated list of targets. For use on the command-line. 
   * 
   * @parameter expression="${target}"
   */
  protected String target;
  
  /**
   * Checks if the packaging type of the current Maven project belongs to the packaging types that, when used, 
   * woint contain the main artifact for the project (if any) in the final package.    
   * @return
   */
  
  protected final boolean packagingTypeBelongsToIgnoreList(){
	boolean inList = false;
	Log l = getLog();
		l.info("ignorePackagingTypes set. Contains: " + ignorePackagingTypes 
				+ " . Project packaging is "+project.getPackaging());
	  for(String s : ignorePackagingTypes.split(",")){
		  if(project.getPackaging().compareToIgnoreCase(s)==0){
			  	inList = true;
			  }
	  }
	  return inList;
  }

}
