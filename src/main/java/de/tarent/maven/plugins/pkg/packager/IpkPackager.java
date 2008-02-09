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

package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.DistroConfiguration;
import de.tarent.maven.plugins.pkg.Packaging;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.generator.ControlFileGenerator;
import de.tarent.maven.plugins.pkg.map.PackageMap;

/**
 * Creates a Debian package file (.deb)
 * 
 */
public class IpkPackager extends Packager
{
  
  public void execute(Log l,
                      Packaging.Helper ph,
                      DistroConfiguration distroConfig,
                      PackageMap packageMap) throws MojoExecutionException
  {
    String packageName = ph.getPackageName();
    String packageVersion = ph.getPackageVersion();

    File controlFile = new File(ph.getBasePkgDir(), "CONTROL/control");
    File srcArtifactFile = ph.getSrcArtifactFile();

    // A set which will be filled with the artifacts which need to be bundled with the
    // application.
    Set bundledArtifacts = null;
    StringBuilder bcp = new StringBuilder();
    StringBuilder cp = new StringBuilder();
    
    long byteAmount = srcArtifactFile.length();

    try
      {
    	// The following section does the coarse-grained steps
    	// to build the package(s). It is meant to be kept clean
    	// from simple Java statements. Put additional functionality
    	// into the existing methods or create a new one and add it
    	// here.
    	
        ph.prepareInitialDirectories();

        ph.copyProjectArtifact();
        
        byteAmount += ph.copyFiles();

        // Create classpath line, copy bundled jars and generate wrapper
        // start script only if the project is an application.
        if (distroConfig.getMainClass() != null)
          {
            // TODO: Handle native library artifacts properly.
            
            bundledArtifacts = ph.createClasspathLine(bcp, cp);

            ph.generateWrapperScript(bundledArtifacts, bcp.toString(), cp.toString(), false);

            byteAmount += ph.copyArtifacts(bundledArtifacts);
          }
        
        generateControlFile(l,
                            ph,
                            distroConfig,
                            controlFile,
                            packageName,
                            packageVersion,
                            ph.createDependencyLine(),
                            byteAmount);

        createPackage(l, ph, ph.getBasePkgDir());
        
      }
    catch (MojoExecutionException badMojo)
      {
        throw badMojo;
      }
    
    /* When the Mojo fails to complete its task the work directory will be left
     * in an unclean state to make it easier to debug problems.
     * 
     * However the work dir will be cleaned up when the task is run next time.
     */
  }

  /** Validates arguments and test tools.
   * 
   * @throws MojoExecutionException
   */
  public void checkEnvironment(Log l, DistroConfiguration dc) throws MojoExecutionException
  {
    Utils.exec(new String[] { "which", "ipkg-build" },
               "ipkg-build returned with an error. Check your installation!",
               "ipkg-build is not available on this system. Check your installation!");
  }

  /**
   * Creates a control file whose dependency line can be provided as an argument.
   * 
   */
  private void generateControlFile(Log l,
                                   Packaging.Helper ph,
                                   DistroConfiguration dc,
                                   File controlFile,
                                   String packageName,
                                   String packageVersion,
                                   String dependencyLine,
                                   long byteAmount)
      throws MojoExecutionException
  {
	ControlFileGenerator cgen = new ControlFileGenerator();
	cgen.setPackageName(packageName);
	cgen.setVersion(packageVersion);
	cgen.setSection(dc.getSection());
	cgen.setDependencies(dependencyLine);
	cgen.setMaintainer(dc.getMaintainer());
	cgen.setArchitecture(dc.getArchitecture());
    cgen.setOE(packageName + "-" + packageVersion);
    
    String url = ph.getProjectUrl();
    if (url == null)
    {
    	l.warn("Project has no <url> field. However IPK packages require this. Using a dummy for now.");
    	url = "http://not-yet-set.org/" + packageName;
    }
    cgen.setSource(url);
    cgen.setHomepage(url);
    
    String desc = ph.getProjectDescription();
    if (desc == null)
    {
    	l.warn("Project has no <description> field. However IPK packages require this. Using a placeholder for now.");
    	desc = "No description given yet.";
    }
    cgen.setShortDescription(desc);
	    
    l.info("creating control file: " + controlFile.getAbsolutePath());
    Utils.createFile(controlFile, "control");
    
    try
      {
        cgen.generate(controlFile);
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException("IOException while creating control file.",
                                         ioe);
      }

  }

  private void createPackage(Log l, Packaging.Helper ph, File base) throws MojoExecutionException
  {
    l.info("calling ipkg-build to create binary package");
    
    Utils.exec(new String[] {"ipkg-build",
                             "-o",
                             "root",
                             "-g",
                             "root",
                             base.getName(),
                             ph.getOutputDirectory().getAbsolutePath() },
                ph.getTempRoot(),
                "'ipkg-build failed.",
                "Error creating the .ipk file.");
  }

}
