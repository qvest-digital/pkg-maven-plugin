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

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Creates a Debian package file (.deb) for the project. If the project is an application
 * a proper wrapper script is generated.
 * 
 * TODO: Description needs to formatted in a Debian-specific way 
 * 
 * @execute phase="package"
 * @goal deb
 */
public class DebianPackaging extends AbstractManagedPackagingMojo
{

  /**
   * Temporary directory that contains the files to be assembled.
   * 
   * @parameter expression="${project.build.directory}/deb-tmp"
   * @required
   * @readonly
   */
  private File tempRoot;
  
  /**
   * Allows overriding the name of the gcj executable.
   * It is intended to be used from the command-line.
   * 
   * @parameter expression="${gcjExec}" default-value="gcj-4.1"
   * @required
   */
  private String gcjExec;
  
  /**
   * Allows overriding the name of the gcj-dbtool executable.
   * It is intended to be used from the command-line.
   * 
   * @parameter expression="${gcjDbToolExec}" default-value="gcj-dbtool-4.1"
   * @required
   */
  private String gcjDbToolExec;

  /**
   * A String of the form "name <email-address>" (without quotes).
   * 
   * @parameter default-value="tarent GmbH <debian-packages@tarent.de>"
   * @required
   */
  private String debianMaintainer;   

  /**
   * If this is set to true the jar will be precompiled with gcj and
   * everything needed to get this working is added to the package.
   * 
   * @parameter
   */
  private boolean aotCompile;
  
  /**
   * The architecture used for the package. If nothing is
   * specified it defaults to <code>all</code>.
   * 
   * @parameter default-value="all"
   */
  private String debArchitecture;
  
  /**
   * Distribution id for the .deb packaging.
   *  
   * @parameter default-value="debian_etch"
   */
  private String debDistribution;

  public void execute() throws MojoExecutionException
  {
    checkEnvironment();
    
    PackageMap packageMap = new PackageMap(defaultPackageMapURL,
                                           auxPackageMapURL,
                                           distro != null ? distro : debDistribution,
                                           new HashSet());

    String packageName = PackageMap.debianise(artifactId, section);
    String packageVersion = fixVersion(version) + "-0" + (distro != null ? distro : debDistribution);

    /*
     * Not using File.separator in this class because we assume being on a UNIXy machine!
     */

    // All files belonging to the package are put into this directory. The layout inside
    // it is done according to `man dpkg-deb`
    File basePkgDir = new File(tempRoot, packageName + "-" + packageVersion);
    
    // The Debian control file (package name, dependencies etc).
    File controlFile = new File(basePkgDir, "DEBIAN/control");

    // A file pointing at the source jar (it *MUST* be a jar).
    File srcArtifactFile = new File(outputDirectory.getPath(), finalName + ".jar");

    // The destination file for the project's artifact inside the the package.
    File dstArtifactFile = new File(basePkgDir, packageMap.getDefaultJarPath() + "/" + artifactId + ".jar");
    
    // The destination directory for JNI libraries
    File dstJNIDir = new File(basePkgDir, packageMap.getDefaultJNIPath());
    
    // The destination directory for the jars bundled with the project.
    File dstBundledArtifactsDir = new File(basePkgDir, packageMap.getDefaultJarPath() + "/" + artifactId);

    // The wrapper script to launch an application (This is unused for libraries).
    File wrapperScriptFile = new File(basePkgDir, packageMap.getDefaultBinPath() + "/" + artifactId);

    String gcjPackageName = gcjise(artifactId);
    
    // The base directory for the gcj package.
    File aotPkgDir = new File(tempRoot, gcjPackageName + "-" + packageVersion);
    
    // The file extension for aot-compiled binaries.
    String aotExtension = ".jar.so";
    
    // The destination directory for all aot-compiled binaries.
    File aotDstDir = new File(aotPkgDir, packageMap.getDefaultJarPath());
    
    // The file name of the aot-compiled binary of the project's own artifact.
    File aotCompiledBinaryFile = new File(aotDstDir, artifactId + aotExtension);
    
    // The destination directory for all classmap files. 
    File aotDstClassmapDir = new File(aotPkgDir, "usr/share/gcj-4.1/classmap.d");
    
    // The file name of the classmap of the project's own artifact. 
    File aotClassmapFile = new File(aotDstClassmapDir, artifactId + ".db");
    
    // The destination file for the 'postinst' script.
    File aotPostinstFile = new File(aotPkgDir, "DEBIAN/postinst");

    // The destination file for the 'control' file.
    File aotControlFile = new File(aotPkgDir, "DEBIAN/control");
    
    // A set which will be filled with the artifacts which need to be bundled with the
    // application.
    Set bundledArtifacts = new HashSet();
    
    long byteAmount = srcArtifactFile.length();

    Log l = getLog();
    try
      {
    	// The following section does the coarse-grained steps
    	// to build the package(s). It is meant to be kept clean
    	// from simple Java statements. Put additional functionality
    	// into the existing methods or create a new one and add it
    	// here.
    	
        prepareDirectories(l, tempRoot, basePkgDir, dstJNIDir);

        copyArtifact(l, srcArtifactFile, dstArtifactFile);
        
        copyJNILibraries(l, dstJNIDir);
        
        copyResources(l, basePkgDir, resources, "/usr/bin:/usr/local/bin:/sbin");

        // Create classpath line, copy bundled jars and generate wrapper
        // start script only if the project is an application.
        if (mainClass != null)
          {
            // TODO: Handle native library artifacts properly.

            generateWrapperScript(l, packageMap, bundledArtifacts, wrapperScriptFile);

            byteAmount += copyArtifacts(l, bundledArtifacts, dstBundledArtifactsDir);
          }
        
        generateControlFile(l,
        		            controlFile,
        		            packageName,
        		            packageVersion,
        		            createDependencyLine(packageMap),
        		            debArchitecture,
        		            byteAmount);

        createPackage(l, basePkgDir);
        
        if (aotCompile)
          {
            prepareDirectories(l, tempRoot, aotPkgDir, null);
            // At this point anything created in the basePkgDir cannot be used
            // any more as it was removed by the above method.
            
            byteAmount = aotCompiledBinaryFile.length() + aotClassmapFile.length();
            
            AotCompileUtils.compile(l, srcArtifactFile, aotCompiledBinaryFile);
            
            AotCompileUtils.generateClassmap(l,
                                             aotClassmapFile,
                                             srcArtifactFile,
                                             aotCompiledBinaryFile,
                                             packageMap.getDefaultJarPath());

            // AOT-compile and classmap generation for bundled Jar libraries
            // are only needed for applications.
            if (mainClass != null)
              byteAmount += AotCompileUtils.compileAndMap(l,
            		                bundledArtifacts,
            		                aotDstDir,
            		                aotExtension,
            		                aotDstClassmapDir,
                                    packageMap.getDefaultJarPath());
            
/*            gen.setShortDescription(gen.getShortDescription() + " (GCJ version)");
            gen.setDescription("This is the ahead-of-time compiled version of "
                               + "the package for use with GIJ.\n"
                               + gen.getDescription());
 */          
            // The dependencies of a "-gcj" package are always java-gcj-compat
            // and the corresponding 'bytecode' version of the package.
            // GCJ can only compile for one architecture.
            generateControlFile(l,
            		            aotControlFile,
            		            gcjPackageName,
            		            packageVersion,
            		            "java-gcj-compat",
            		            System.getProperty("os.arch"),
            		            byteAmount);
            
            AotCompileUtils.depositPostinstFile(l, aotPostinstFile);
            
            createPackage(l, aotPkgDir);
          }
        
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
  private void checkEnvironment() throws MojoExecutionException
  {
	Log l = getLog();
	l.info("package system           : Debian (.deb)");
    l.info("distribution             : " + (distro != null ? distro : debDistribution));
    l.info("default package map      : " + ((defaultPackageMapURL != null) ? defaultPackageMapURL.toString() : "built-in"));
    l.info("auxiliary package map    : " + ((auxPackageMapURL != null) ? auxPackageMapURL.toString() : "no"));
	l.info("type of project          : " + ((mainClass != null) ? "application" : "library"));
	l.info("Debian section           : " + section);
	l.info("bundle all dependencies  : " + ((bundleDependencies) ? "yes" : "no"));
	l.info("ahead of time compilation: " + ((aotCompile) ? "yes" : "no"));
	l.info("JNI libraries            : " + ((jniLibraries == null) ? "none" : String.valueOf(jniLibraries.size())));
	
	if (aotCompile)
	  {
	    l.info("aot compiler             : " + gcjExec);
	    l.info("aot classmap generator   : " + gcjDbToolExec);
	  }

	if (mainClass == null)
	  {
		  if (!section.equals("libs"))
			  throw new MojoExecutionException("<debianSection> should be libs if no main class is given.");
		  
		  if (bundleDependencies)
			  throw new MojoExecutionException("bundling dependencies to a library makes no sense.");
	  }
	  else
	  {
        if (section.equals("libs"))
          throw new MojoExecutionException("Set a proper debian section if main class parameter is set.");
	  }
    
    if (aotCompile)
      {
        AotCompileUtils.setGcjExecutable(gcjExec);
        AotCompileUtils.setGcjDbToolExecutable(gcjDbToolExec);
        
        AotCompileUtils.checkToolAvailability();
      }
    
  }

  /**
   * Creates a control file whose dependency line can be provided as an argument.
   * 
   * @param l
   * @param controlFile
   * @param packageName
   * @param packageVersion
   * @param installedSize
   * @throws MojoExecutionException
   */
  private void generateControlFile(Log l, File controlFile, String packageName, String packageVersion, String dependencyLine, String architecture, long byteAmount)
      throws MojoExecutionException
  {
	ControlFileGenerator cgen = new ControlFileGenerator();
	cgen.setPackageName(packageName);
	cgen.setVersion(packageVersion);
	cgen.setSection(section);
	cgen.setDependencies(dependencyLine);
	cgen.setMaintainer(debianMaintainer);
	cgen.setShortDescription(project.getDescription());
	cgen.setDescription(project.getDescription());
	cgen.setArchitecture(architecture);
	cgen.setInstalledSize(getInstalledSize(byteAmount));
	    
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

  private void createPackage(Log l, File basePkgDir) throws MojoExecutionException
  {
    l.info("calling dpkg-deb to create binary package");
    
    Utils.exec(new String[] {"dpkg-deb",
                             "--build",
                             basePkgDir.getName(),
                             outputDirectory.getAbsolutePath() },
                tempRoot,
                "'dpkg --build' failed.",
                "Error creating the .deb file.");
  }
 
 /**
   * Convert the artifactId into a Debian package name which contains
   * gcj precompiled binaries.
   * 
   * @param artifactId
   * @return
   */
  private String gcjise(String artifactId)
  {
    return section.equals("libs") ? "lib" + artifactId + "-gcj"
                                        : artifactId + "-gcj";
    
  }

  /** Converts a byte amount to the unit used by the Debian control file
   * (usually KiB). That value can then be used in a ControlFileGenerator
   * instance.
   * 
   * @param byteAmount
   * @return
   */
  private long getInstalledSize(long byteAmount)
  {
    return byteAmount / 1024L;
  }


}
