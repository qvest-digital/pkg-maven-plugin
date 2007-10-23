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

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * 
 * @execute phase="package"
 * @goal izpack
 */
public class IzPackPackaging extends AbstractPackagingMojo
{
  private static final String IZPACK_EMBEDDED_JAR = "izpack-embedded.jar";
  
  private static final String FIX_CLASSMAP_JAR = "fix-classmap-1.0.jar";
  
  private static final String FIX_CLASSMAP_SCRIPT = "fix-classmap";
  
  private static final String STARTER_CLASS = "_Starter.class";
  
  private static final String CLASSPATH = "_classpath";

  /**
   * @parameter expression="${project.build.finalName}"
   * @required
   * @readonly
   */
  private String finalName;

  /**
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  private File outputDirectory;

  /**
   * Temporary directory that contains the files to be assembled.
   * 
   * @parameter expression="${project.build.directory}/izpack-tmp"
   * @required
   * @readonly
   */
  private File tempRoot;
  
  /**
   * Denotes the name of the IzPack XML descriptor file.
   * 
   * @parameter default-value="installer.xml"
   * @required
   */
  private String installerXml;
  
  /**
   * Denote the name of the wrapper script. If nothing is specified
   * this is identical to the artifactId.
   * 
   * @parameter expression="${project.artifactId}"
   */
  private String wrapperScriptName;

  /**
   * A long, possibly multiple-line spanning description of the package. Do not
   * apply formatting other than line breaks with this text because it will
   * have to be formatted for the packaging system automatically.
   * 
   * @parameter expression="${project.description}" default-value="No full description yet".
   */
  private String description;

  /**
   * The main class parameter is used to generate a proper wrapper script
   * which is passed to the IzPack installer generator.
   * 
   * @parameter
   * @required
   */
  private String mainClass;

  /**
   * maximum heap space for the jvm
   * 
   * @parameter
   */
  private String maxJavaMemory;
  
  /**
   * If set the application will be precompiled with gcj. Neccessary entries
   * to the IzPack installer XML file will be added automatically.
   * 
   * @parameter
   */
  private boolean aotCompile;

  /**
   * @parameter expression="${javaExec}" default-value="java"
   * @required
   * 
   */
  private String javaExec;
  
  /**
   * Allows overriding the name of the gcj executable.
   * 
   * @parameter expression="${gcjExec}" default-value="gcj-4.1"
   * @required
   */
  private String gcjExec;
  
  /**
   * Allows overriding the name of the gcj-dbtool executable.
   * 
   * @parameter expression="${gcjDbToolExec}" default-value="gcj-dbtool-4.1"
   * @required
   */
  private String gcjDbToolExec;
  
  /**
   * A directory inside "src/main" which should be copied into the mojo's
   * working dir. This allows accessing the contents from within the
   * IzPack installer descriptor.
   * 
   * @parameter
   */
  private String copyDir;
  
  /**
   * If set the installer will generate a special starter class and startscript
   * which works around platform limitations like fixed command-line lengths.
   * 
   * @parameter
   */
  private boolean advancedStarter;
  
  /**
   * A set of properties which will be added to the wrapper scripts runtime start
   * argument.
   * 
   * @parameter 
   */
  private Properties izpackProperties;
  
  public void execute() throws MojoExecutionException
  {
    checkEnvironment();
    
    File artifactFile = new File(outputDirectory.getPath(), finalName + ".jar");
    
    // The destination file for the embedded IzPack installation.
    File izPackEmbeddedJarFile = new File(tempRoot, IZPACK_EMBEDDED_JAR);
    
    // The directory in which the embedded IzPack installation is unpacked
    // at runtime.
    File izPackEmbeddedRoot = new File(tempRoot, "izpack-embedded");
    
    // The root directory containing the IzPack installer XML file and
    // all accompanying resource files.
    File srcRoot = new File(project.getBasedir(), "src/main/izpack");
    
    // The root directory into which everything from srcRoot is copied
    // into (inside the outputDirectory).
    File tempDescriptorRoot = new File(tempRoot, "descriptor");
    
    // The root directory into which the jars from the dependencies
    // are put.
    File libraryRoot = new File(tempDescriptorRoot, "lib");
    
    // The root directory into which the starter and the classpath
    // properties file are put.
    File starterRoot = new File(tempDescriptorRoot, "_starter");
    
    // The directory inside src/main which is to be copied into the temp
    // descriptor root directory.
    File copyDirRoot = (copyDir != null) ? new File(project.getBasedir(), "src/main/" + copyDir) : null;
    
    // The XML file for IzPack which describes how to generate the installer. 
    File installerXmlFile = new File(tempDescriptorRoot, installerXml);
    File modifiedInstallerXmlFile = new File(tempDescriptorRoot, "modified-" + installerXml);
    
    // The resulting Jar file which contains the runnable installer.
    File resultFile = new File(outputDirectory, finalName + "-installer.jar");
    
    // This is only neccessary when a wrapper script should be created
    File wrapperScriptFile = new File(tempDescriptorRoot, wrapperScriptName);
    File windowsWrapperScriptFile = new File(tempDescriptorRoot, wrapperScriptName + ".bat");
    
    // The following is only neccessary when aot compilation is enabled.
    File aotRoot = new File(tempDescriptorRoot, "aot");
    File aotCompiledBinaryFile = new File(aotRoot, finalName + ".jar.so");
    File aotClassmapFile = new File(aotRoot, finalName + ".db");
    File fixClassmapJarFile = new File(tempDescriptorRoot, FIX_CLASSMAP_JAR);
    File fixClassmapScriptFile = new File(tempDescriptorRoot, FIX_CLASSMAP_SCRIPT);
    
    String libraryPrefix = "%{INSTALL_PATH}/" + libraryRoot.getName();  
    String aotPrefix = "%{INSTALL_PATH}/" + aotRoot.getName();
    String starterPrefix = "%{INSTALL_PATH}/" + starterRoot.getName(); 
    
    Log l = getLog();
    Set deps = null;
    
    try
      {
        checkEnvironment();
        
        prepareDirectories(l, izPackEmbeddedRoot, srcRoot, tempDescriptorRoot, libraryRoot, aotRoot);
        
        unpackIzPack(l, izPackEmbeddedJarFile, izPackEmbeddedRoot);
        
        deps = copyDependencies(l, libraryRoot, artifactFile);
        
        if (copyDirRoot != null)
         copyDir(l, copyDirRoot, tempDescriptorRoot);

        l.info("parsing installer xml file: " + installerXmlFile);
        IzPackDescriptor desc = new IzPackDescriptor(installerXmlFile, "Unable to parse installer xml file.");

        l.info("adding/modifying basic information");
        desc.fillInfo(l, artifactId, project.getVersion(), project.getUrl());
        
        if (aotCompile)
          {
            AotCompileUtils.compile(l, 
                                    artifactFile,
                                    aotCompiledBinaryFile);

            AotCompileUtils.generateClassmap(l,
                                             aotClassmapFile,
                                             artifactFile,
                                             aotCompiledBinaryFile,
                                             "");
            
            prepareAotInstallation(l,
                                           desc,
                                           aotRoot,
                                           fixClassmapJarFile,
                                           fixClassmapScriptFile);
          }
        else
          {
            desc.removeAotPack();
          }
        
        WrapperScriptGenerator gen = createWrapperScriptGenerator(libraryPrefix);
        
        if (advancedStarter)
          {
        	l.info("setting up advanced starter");
        	setupStarter(l, starterRoot, deps, artifactFile, libraryPrefix);
        	
        	assignStarterClasspath(gen, desc, starterPrefix);
          }
        else
          {
        	l.info("using traditional starter");
        	assignTraditionalClasspath(gen, libraryPrefix, libraryRoot);
        	
        	if (aotCompile)
        	  setupAotStartscript(l, gen, aotPrefix, aotClassmapFile, aotCompiledBinaryFile);
        	
          }
        
        generateWrapperScripts(gen, wrapperScriptFile, windowsWrapperScriptFile);

        l.info("adding wrapper script information.");
        desc.addUnixWrapperScript(wrapperScriptFile.getName(), description);
        desc.addWindowsWrapperScript(windowsWrapperScriptFile.getName(), description);
        
        l.info("writing modified installer xml file.");
        desc.finish(modifiedInstallerXmlFile, "Unable to write modified installer xml file.");
        
        createInstaller(l,
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
  
  private void copyDir(Log l, File src, File dst) throws MojoExecutionException
  {
	  l.info("copying additional directory: " + src.getAbsolutePath() + " .");
	  
	  try
	  {
		  FileUtils.copyDirectoryStructure(src, new File(dst, src.getName()));
	  }
	  catch (IOException ioe)
	  {
		  throw new MojoExecutionException("Error copying additional directory", ioe);
	  }

  }

  /** Validates arguments and tests tools.
   * 
   * @throws MojoExecutionException
   */
  private void checkEnvironment() throws MojoExecutionException
  {
    if (aotCompile)
      {
        AotCompileUtils.setGcjExecutable(gcjExec);
        AotCompileUtils.setGcjDbToolExecutable(gcjDbToolExec);
        
        AotCompileUtils.checkToolAvailability();
      }
  }

  /**
   * Creates the temporary and package base directory.
   * 
   * @param l
   * @param basePkgDir
   * @throws MojoExecutionException
   */
  private void prepareDirectories(Log l, File izPackEmbeddedRoot,
                                  File srcDir,
                                  File tempDescriptorRoot,
                                  File libraryRoot,
                                  File aotRoot)
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

    l.info("creating directory for ahead of time compilation: " + aotRoot.getAbsolutePath());
    if (!aotRoot.mkdirs())
      throw new MojoExecutionException("Could not create directory for aot compilation.");

  }
  
  /**
   * Copies the project's dependency artifacts as well as the main artifact
   * of the project itself.
   * 
   * <p>The set of dependency artifacts and the project's artifact are then
   * returned.</p>
   * 
   * @param l
   * @param libraryRoot
   * @param artifactFile
   * @return
   * @throws MojoExecutionException
   */
  private Set copyDependencies(Log l, File libraryRoot, File artifactFile)
  throws MojoExecutionException
  {
    l.info("retrieving dependencies");
    Set artifacts = null;
    try
    {
      artifacts = findArtifacts();
    }
    catch (ArtifactNotFoundException e)
    {
      throw new MojoExecutionException("Unable to retrieve artifact.", e);
    }
    catch (ArtifactResolutionException e)
      {
        throw new MojoExecutionException("Unable to resolve artifact.", e);
      }
    catch (ProjectBuildingException e)
      {
        throw new MojoExecutionException("Unable to build project.", e);
      }
    catch (InvalidDependencyVersionException e)
      {
        throw new MojoExecutionException("Invalid dependency version.", e);
      }
    
    copyArtifacts(l, artifacts, libraryRoot);
    
    try
    {
      // Copies the project's own artifact into the library root directory.
      l.info("copying project's artifact file: " + artifactFile.getAbsolutePath());
      FileUtils.copyFileToDirectory(artifactFile, libraryRoot);
    }
    catch (IOException ioe)
    {
      throw new MojoExecutionException("IOException while copying project's artifact file.");
    }

    return artifacts;
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
    Utils.storeInputStream(IzPackPackaging.class.getResourceAsStream(IZPACK_EMBEDDED_JAR),
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
  private WrapperScriptGenerator createWrapperScriptGenerator(String libraryPrefix)
  {
    WrapperScriptGenerator gen = new WrapperScriptGenerator();
	    
	// Puts the project specific system properties into the wrapper script.
	if (izpackProperties != null)
	  gen.setProperties(izpackProperties);

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
  private void setupStarter(Log l, File starterRoot, Set dependencies, File artifactFile, String libraryPrefix)
  throws MojoExecutionException
  {
	File destStarterClassFile = new File(starterRoot, STARTER_CLASS);

	Utils.createFile(destStarterClassFile, "starter class");
    Utils.storeInputStream(IzPackPackaging.class.getResourceAsStream("/" + STARTER_CLASS),
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
		  IzPackDescriptor desc,
		  String starterPrefix)
  throws MojoExecutionException
  {
  	// Sets main class and classpath for the wrapper script.
  	gen.setMainClass("_Starter");
  	gen.setClasspath(starterPrefix);
    gen.setMaxJavaMemory(maxJavaMemory);

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
  private void assignTraditionalClasspath(WrapperScriptGenerator gen, String libraryPrefix, File libraryRoot)
  throws MojoExecutionException
  {
    gen.setMainClass(mainClass);

    // All Jars have to reside inside the libraryRoot.
	gen.setClasspath(createClasspath(":", libraryRoot, libraryPrefix));
  }
 
  private void setupAotStartscript(Log l,
		                             WrapperScriptGenerator gen,
                                     String aotPrefix,
                                     File aotClassmapFile,
                                     File aotBinaryFile)
  throws MojoExecutionException
  {
    gen.setClassmapFile(aotPrefix + "/" + aotClassmapFile.getName());
    gen.setCustom(createFixClassmapScriptEntry(aotPrefix,
                                               gen.getClassmapFile(),
                                               aotPrefix + "/" + aotBinaryFile.getName()));
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
  
  private void prepareAotInstallation(Log l, IzPackDescriptor desc,
                                              File aotRoot,
                                              File fixClassmapJarFile, File fixClassmapScriptFile)
  throws MojoExecutionException
  {
    l.info("storing fix-classmap jar: " + fixClassmapJarFile.getAbsolutePath());
    Utils.storeInputStream(IzPackPackaging.class.getResourceAsStream(FIX_CLASSMAP_JAR),
                           fixClassmapJarFile,
                           "IOException while storing fix-classmap jar file.");
    
    l.info("storing fix-classmap script: " + fixClassmapScriptFile.getAbsolutePath());
    Utils.storeInputStream(IzPackPackaging.class.getResourceAsStream(FIX_CLASSMAP_SCRIPT),
                           fixClassmapScriptFile,
                           "IOException while storing fix-classmap shell script.");
                           
    l.info("adding Ahead Of Time compilation installation pack.");
    desc.addAotPack(aotRoot.getName(),
                    fixClassmapJarFile.getName(),
                    fixClassmapScriptFile.getName());

  }

  private String createFixClassmapScriptEntry(String prefix, String classmapPath, String binaryPath)
  {
    String newClassmapPath = classmapPath + ".NEW";
    String repaired = prefix + "/repaired";
    
    return "if [ ! -f " + repaired + " ]; then\n" +
     "\tsh fix-classmap " + classmapPath + " " + newClassmapPath + " " + binaryPath + " &&\n" +
     "\tmv " + newClassmapPath + " " + classmapPath + "\n" +
     "\techo \"The classmap file is believed to be fixed. Remove this file to repair it again.\" > " + repaired + "\n" +
     "fi\n";
  }
  
}
