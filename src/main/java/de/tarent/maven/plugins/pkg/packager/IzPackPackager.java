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
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import de.tarent.maven.plugins.pkg.DistroConfiguration;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.generator.WrapperScriptGenerator;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class IzPackPackager extends Packager
{
  private static final String IZPACK_EMBEDDED_JAR = "izpack-embedded.jar";
  
  private static final String STARTER_CLASS = "_Starter.class";
  
  private static final String CLASSPATH = "_classpath";
  
  public void execute(Log l,
                      PackagerHelper ph,
                      DistroConfiguration distroConfig,
                      PackageMap packageMap) throws MojoExecutionException
  {
    File artifactFile = new File(ph.getOutputDirectory(), ph.getPackageName() + ".jar");
    
    // The destination file for the embedded IzPack installation.
    File izPackEmbeddedJarFile = new File(ph.getTempRoot(), IZPACK_EMBEDDED_JAR);
    
    // The directory in which the embedded IzPack installation is unpacked
    // at runtime.
    File izPackEmbeddedRoot = new File(ph.getTempRoot(), "izpack-embedded");
    
    // The root directory containing the IzPack installer XML file and
    // all accompanying resource files.
    File srcRoot = ph.getIzPackSrcDir();
    
    // The root directory into which everything from srcRoot is copied
    // into (inside the outputDirectory).
    File tempDescriptorRoot = new File(ph.getTempRoot(), "descriptor");
    
    // The root directory into which the jars from the dependencies
    // are put.
    File dstBundledArtifactsDir = new File(tempDescriptorRoot, "lib");
    
    // The root directory into which the starter and the classpath
    // properties file are put.
    File starterRoot = new File(tempDescriptorRoot, "_starter");
    
    // The XML file for IzPack which describes how to generate the installer. 
    File installerXmlFile = new File(tempDescriptorRoot, distroConfig.getIzPackInstallerXml());
    File modifiedInstallerXmlFile = new File(tempDescriptorRoot, "modified-" + distroConfig.getIzPackInstallerXml());
    
    // The resulting Jar file which contains the runnable installer.
    File resultFile = new File(ph.getOutputDirectory(), ph.getPackageName() + "-installer.jar");
    
    // This is only neccessary when a wrapper script should be created
    File wrapperScriptFile = ph.getWrapperScriptFile(tempDescriptorRoot);
    File windowsWrapperScriptFile = new File(wrapperScriptFile.getAbsolutePath() + ".bat");
    
    // The following is only neccessary when aot compilation is enabled.
    String libraryPrefix = "%{INSTALL_PATH}/" + dstBundledArtifactsDir.getName();  
    String starterPrefix = "%{INSTALL_PATH}/" + starterRoot.getName(); 
    
    Set deps = null;
    
    try
      {
        
        prepareDirectories(l, izPackEmbeddedRoot, srcRoot, tempDescriptorRoot, dstBundledArtifactsDir, null);
        
        unpackIzPack(l, izPackEmbeddedJarFile, izPackEmbeddedRoot);
        
        deps = ph.copyDependencies(dstBundledArtifactsDir, artifactFile);

        Utils.copyAuxFiles(l, ph.getDefaultAuxFileSrcDir(), tempDescriptorRoot, distroConfig.getAuxFiles());

        l.info("parsing installer xml file: " + installerXmlFile);
        IzPackDescriptor desc = new IzPackDescriptor(installerXmlFile, "Unable to parse installer xml file.");

        l.info("adding/modifying basic information");
        desc.fillInfo(l, ph.getPackageName(), ph.getPackageVersion(), ph.getProjectUrl());
        
        desc.removeAotPack();
        
        WrapperScriptGenerator gen = createWrapperScriptGenerator(libraryPrefix, distroConfig);
        
        if (distroConfig.isAdvancedStarter())
          {
        	l.info("setting up advanced starter");
        	setupStarter(l, distroConfig.getMainClass(), starterRoot, deps, artifactFile, libraryPrefix);
        	
        	assignStarterClasspath(gen, distroConfig, desc, starterPrefix);
          }
        else
          {
        	l.info("using traditional starter");
        	assignTraditionalClasspath(gen, distroConfig, libraryPrefix, dstBundledArtifactsDir);
          }
        
        generateWrapperScripts(gen, wrapperScriptFile, windowsWrapperScriptFile);

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
      FileUtils.copyDirectoryStructure(srcDir, tempDescriptorRoot);
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
  
  /**
   * Creates a {@link WrapperScriptGenerator} instance and initializes
   * with basic values.
   * 
   * @param libraryPrefix
   * @return
   */
  private WrapperScriptGenerator createWrapperScriptGenerator(String libraryPrefix, DistroConfiguration dc)
  {
    WrapperScriptGenerator gen = new WrapperScriptGenerator();
	    
	// Puts the project specific system properties into the wrapper script.
    gen.setProperties(dc.getSystemProperties());

	// Sets a library path that assumes that the JNI libraries
    // lie in the libraryRoot. %{INSTALL_PATH} is replaced with
    // the actual installation location by IzPack.
    gen.setLibraryPath(libraryPrefix);

	return gen;
  }
  
  /**
   * Copies the starter classfile to the starter root, prepares the
   * classpath properties file and stores it at that root, too.
   * 
   * @param starterRoot
   * @param dependencies
   * @param libraryPrefix
   * @throws MojoExecutionException
   */
  private void setupStarter(Log l, String mainClass, File starterRoot, Set dependencies, File artifactFile, String libraryPrefix)
  throws MojoExecutionException
  {
	File destStarterClassFile = new File(starterRoot, STARTER_CLASS);

	Utils.createFile(destStarterClassFile, "starter class");
    Utils.storeInputStream(IzPackPackager.class.getResourceAsStream("/" + STARTER_CLASS),
    		               destStarterClassFile, "Unable to store starter class file in destination.");
    
    File destClasspathFile = new File(starterRoot, CLASSPATH); 
    Utils.createFile(destClasspathFile, "starter classpath");

    PrintWriter writer = null;
    try
      {
        writer = new PrintWriter(destClasspathFile);
        
        writer.println("# This file controls the application's classpath and is autogenerated.");
        writer.println("# Slashes (/) in the filenames are replaced with the platform-specific");
        writer.println("# separator char at runtime.");
        writer.println("# The next line is the fully-classified name of the main class:");
        writer.println(mainClass);
        writer.println("# The following lines are the classpath entries:");
        int i = 0;
        Iterator ite = dependencies.iterator();
    
        while (ite.hasNext())
          {
    	    Artifact a = (Artifact) ite.next();
    	    String entry = libraryPrefix + "/" + a.getFile().getName();
    	    writer.println(entry);
    	    i++;
          }
        writer.println(libraryPrefix + "/" + artifactFile.getName());
        i++;
    
        l.info("created " + i + " library entries");
      }
    catch (IOException e)
      {
    	throw new MojoExecutionException("storing the classpath entries failed", e);
      }
    finally
      {
    	if (writer != null)
    		writer.close();
      }
  }
  
  private void assignStarterClasspath(WrapperScriptGenerator gen,
                                      DistroConfiguration dc,
                                      IzPackDescriptor desc,
                                      String starterPrefix)
  throws MojoExecutionException
  {
  	// Sets main class and classpath for the wrapper script.
  	gen.setMainClass("_Starter");
  	gen.setClasspath(starterPrefix);
    
    gen.setMaxJavaMemory(dc.getMaxJavaMemory());

  	desc.addStarter("_starter", CLASSPATH);
  }
  
  /**
   * Assigns a classpath and the main class in the way Java programs are
   * started traditionally.
   * 
   * <p>That means the applications mainclass and classpath is provided as
   * an argument to the command-line that starts the virtual machine.</p> 
   * 
   * @param gen
   * @param libraryPrefix
   * @param libraryRoot
   * @throws MojoExecutionException
   */
  private void assignTraditionalClasspath(WrapperScriptGenerator gen,
                                          DistroConfiguration dc,
                                          String libraryPrefix,
                                          File libraryRoot)
  throws MojoExecutionException
  {
    gen.setMainClass(dc.getMainClass());
    gen.setMaxJavaMemory(dc.getMaxJavaMemory());

    // All Jars have to reside inside the libraryRoot.
	gen.setClasspath(createClasspath(":", libraryRoot, libraryPrefix));
  }
  
  /**
   * Generates the wrapperscripts according to the way the {@link WrapperScriptGenerator}
   * was configured.
   * 
   * @param gen
   * @param wrapperScriptFile
   * @param windowsWrapperScriptFile
   * @throws MojoExecutionException
   */
  private void generateWrapperScripts(WrapperScriptGenerator gen, File wrapperScriptFile, File windowsWrapperScriptFile)
    throws MojoExecutionException
  {
    
    Utils.createFile(wrapperScriptFile, "wrapper script");
    
    try
    {
      gen.generate(wrapperScriptFile);
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while generating wrapper script.");
    }

    Utils.createFile(windowsWrapperScriptFile, "Windows wrapper script");
    
    try
    {
      gen.generateBatchFile(windowsWrapperScriptFile);
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while generating Windows wrapper script.");
    }
    
  }
  
  private String createClasspath(String delimiter, File libraryRoot, String prefix)
  throws MojoExecutionException
  {
    StringBuilder b = new StringBuilder();
   
    try
    {
      List files = FileUtils.getFiles(libraryRoot, "*.jar", null);
      
      boolean first = true;
      
      for (Iterator ite = files.iterator(); ite.hasNext(); )
        {
          if (first)
            first = false;
          else 
            b.append(delimiter);
            
          File f = (File) ite.next();
          b.append(prefix + "/" + f.getName());
        }
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while listing libraries from directory.");
    }
    
    return b.toString();
  }
   
}
