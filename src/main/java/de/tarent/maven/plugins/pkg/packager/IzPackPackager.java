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

/* $Id: IzPackPackaging.java,v 1.22 2007/08/07 11:29:59 robert Exp $
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

package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.DistroConfiguration;
import de.tarent.maven.plugins.pkg.Packaging;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class IzPackPackager extends Packager
{
  private static final String IZPACK_EMBEDDED_JAR = "izpack-embedded.jar";
  
  public void execute(Log l,
                      Packaging.Helper ph,
                      DistroConfiguration distroConfig,
                      PackageMap packageMap) throws MojoExecutionException
  {
    // The root directory into which everything from srcRoot is copied
    // into (inside the outputDirectory).
    File tempDescriptorRoot = new File(ph.getTempRoot(), "descriptor");
    ph.setBasePkgDir(tempDescriptorRoot);
    
    // The root directory into which the jars from the dependencies
    // are put.
    ph.setDstBundledArtifactsDir(new File(tempDescriptorRoot, "lib"));
    File dstBundledArtifactsDir = ph.getDstBundledArtifactsDir();
    
    // Sets the location of the jar files. As it contains a substituatio variable
    // dstBundledArtifactsDir has been set already (prevents the use of targetJarPath
    // for IO operations).
    ph.setTargetJarDir(new File("%{INSTALL_PATH}", "lib"));
    
    // Sets where to copy the JNI libraries
    ph.setDstJNIDir(new File(tempDescriptorRoot, "lib"));
    
    // Sets which path to use for JNI libraries in scripts.
    ph.setTargetJNIDir(new File("%{INSTALL_PATH}", "lib"));

    // Overrides default dst artifact file.
    ph.setDstArtifactFile(new File(ph.getDstBundledArtifactsDir(), ph.getArtifactId() + ".jar"));
    
    // The destination file for the embedded IzPack installation.
    File izPackEmbeddedJarFile = new File(ph.getTempRoot(), IZPACK_EMBEDDED_JAR);
    
    // The directory in which the embedded IzPack installation is unpacked
    // at runtime.
    File izPackEmbeddedRoot = new File(ph.getTempRoot(), "izpack-embedded");
    
    // The root directory containing the IzPack installer XML file and
    // all accompanying resource files.
    File srcRoot = ph.getIzPackSrcDir();
    
    // The root directory into which the starter and the classpath
    // properties file are put.
    ph.setDstStarterDir(new File(tempDescriptorRoot, "_starter"));
    ph.setTargetStarterDir(new File("%{INSTALL_PATH}", "_starter"));
    
    // The XML file for IzPack which describes how to generate the installer. 
    File installerXmlFile = new File(tempDescriptorRoot, distroConfig.getIzPackInstallerXml());
    File modifiedInstallerXmlFile = new File(tempDescriptorRoot, "modified-" + distroConfig.getIzPackInstallerXml());
    
    // The resulting Jar file which contains the runnable installer.
    File resultFile = new File(ph.getOutputDirectory(), ph.getPackageName() + "-" + ph.getPackageVersion() + "-installer.jar");
    
    // This is only neccessary when a wrapper script should be created
    ph.setWrapperScriptFile(new File(tempDescriptorRoot, (distroConfig.getWrapperScriptName() != null ? distroConfig.getWrapperScriptName() : ph.getArtifactId())));
    File wrapperScriptFile = ph.getWrapperScriptFile();
    File windowsWrapperScriptFile = ph.getWindowsWrapperScriptFile();
    
    Set bundledArtifacts = null;
    StringBuilder bcp = new StringBuilder();
    StringBuilder cp = new StringBuilder();
    
    try
      {
        prepareDirectories(l,
                           ph.getTempRoot(),
                           izPackEmbeddedRoot,
                           srcRoot,
                           tempDescriptorRoot,
                           dstBundledArtifactsDir);
        
        unpackIzPack(l, izPackEmbeddedJarFile, izPackEmbeddedRoot);
        
        bundledArtifacts = ph.createClasspathLine(bcp, cp);
        
        ph.copyProjectArtifact();
        
        ph.copyArtifacts(bundledArtifacts);
        
        ph.copyJNILibraries();

        Utils.copyAuxFiles(l, ph.getAuxFileSrcDir(), tempDescriptorRoot, distroConfig.getAuxFiles());

        l.info("parsing installer xml file: " + installerXmlFile);
        IzPackDescriptor desc = new IzPackDescriptor(installerXmlFile, "Unable to parse installer xml file.");

        l.info("adding/modifying basic information");
        desc.fillInfo(l, ph.getPackageName(), ph.getPackageVersion(), ph.getProjectUrl());
        
        desc.removeAotPack();

        ph.generateWrapperScript(bundledArtifacts, bcp.toString(), cp.toString(), true);
        
        if (distroConfig.isAdvancedStarter())
          desc.addStarter("_starter", "_classpath");

        l.info("adding wrapper script information.");
        desc.addUnixWrapperScript(wrapperScriptFile.getName(), ph.getProjectDescription());
        desc.addWindowsWrapperScript(windowsWrapperScriptFile.getName(), ph.getProjectDescription());
        
        l.info("writing modified installer xml file.");
        desc.finish(modifiedInstallerXmlFile, "Unable to write modified installer xml file.");
        
        createInstaller(l,
                        ph.getJavaExec(),
                        izPackEmbeddedRoot,
                        tempDescriptorRoot,
                        modifiedInstallerXmlFile,
                        resultFile);
        
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

  /** Validates arguments and tests tools.
   * 
   * @throws MojoExecutionException
   */
  public void checkEnvironment(Log l, DistroConfiguration dc) throws MojoExecutionException
  {
    // Nothing to check for.
  }

  /**
   * Creates the temporary and package base directory.
   * 
   * @param l
   * @param basePkgDir
   * @throws MojoExecutionException
   */
  private void prepareDirectories(Log l,
                                  File tempRoot,
                                  File izPackEmbeddedRoot,
                                  File srcDir,
                                  File tempDescriptorRoot,
                                  File libraryRoot)
      throws MojoExecutionException
  {
    l.info("creating temporary directory: " + tempRoot.getAbsolutePath());

    if (!tempRoot.exists() && !tempRoot.mkdirs())
      throw new MojoExecutionException("Could not create temporary directory.");

    l.info("cleaning the temporary directory");
    try
    {
      FileUtils.cleanDirectory(tempRoot);
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("Exception while cleaning temporary directory.",
                                       ioe);
    }

    l.info("creating IzPack base directory: " + izPackEmbeddedRoot.getAbsolutePath());
    if (!izPackEmbeddedRoot.mkdirs())
      throw new MojoExecutionException("Could not create directory for the embedded IzPack installation.");
    
    if (!tempDescriptorRoot.mkdirs())
      throw new MojoExecutionException("Could not create base directory for the IzPack descriptor.");
    
    l.info("copying IzPack descriptor data");
    try
    {
      FileUtils.copyDirectory(srcDir, tempDescriptorRoot, Utils.FILTER);
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while copying IzPack descriptor data.");
    }
    
    l.info("creating directory for dependencies: " + libraryRoot.getAbsolutePath());
    if (!libraryRoot.mkdirs())
      throw new MojoExecutionException("Could not create directory for the dependencies.");

  }
  
  /**
   * Puts the embedded izpack jar from the (resource) classpath into the work directory
   * and unpacks it there.
   * 
   * @param l
   * @param izPackEmbeddedFile
   * @param izPackEmbeddedHomeDir
   * @throws MojoExecutionException
   */
  private void unpackIzPack(Log l, File izPackEmbeddedFile, File izPackEmbeddedHomeDir)
  throws MojoExecutionException
  {
    l.info("storing embedded IzPack installation in " + izPackEmbeddedFile);
    Utils.storeInputStream(IzPackPackager.class.getResourceAsStream(IZPACK_EMBEDDED_JAR),
                           izPackEmbeddedFile,
                           "IOException while unpacking embedded IzPack installation.");
    
    l.info("unzipping embedded IzPack installation to" + izPackEmbeddedHomeDir);
    int count = 0;
    try
    {
    ZipFile zip = new ZipFile(izPackEmbeddedFile);
    Enumeration e = zip.entries();
    
    while (e.hasMoreElements())
      {
        count++;
        ZipEntry entry = (ZipEntry) e.nextElement();
        File unpacked = new File(izPackEmbeddedHomeDir,
                                   entry.getName());
        if (entry.isDirectory())
          unpacked.mkdirs(); // TODO: Check success.
        else
          {
            Utils.createFile(unpacked, "Unable to create ZIP file entry ");
            Utils.storeInputStream(zip.getInputStream(entry), unpacked,
                                   "IOException while unpacking ZIP file entry.");
          }
            
      }
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while unpacking embedded IzPack installation.");
    }
    
    l.info("unpacked " + count + " entries");
  }

  private void createInstaller(Log l,
                               String javaExec,
                               File izPackHomeDir,
                               File izPackBaseDir,
                               File izPackDescriptorFile,
                               File izPackInstallerFile) throws MojoExecutionException
  {
    l.info("calling IzPack compiler to create installer package");
    
    // Command-line argument ordering and naming suitable for IzPack 3.9.0
    Utils.exec(new String[] { 
                             javaExec,
                             "-jar",
                             izPackHomeDir.getAbsolutePath() + "/lib/compiler.jar",
                             izPackDescriptorFile.getAbsolutePath(),
                             "-h",
                             izPackHomeDir.getAbsolutePath(),
                             "-b",
                             izPackBaseDir.getAbsolutePath(),
                             "-o",
                             izPackInstallerFile.getAbsolutePath()
    }, izPackHomeDir,
    "Unable to run IzPack.",
    "IOException while trying to run IzPack.");
  
  }
 
 
   
}
