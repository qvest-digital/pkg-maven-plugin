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

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Creates a IPK package file (.ipk) for the project. If the project is an application
 * a proper wrapper script is generated.
 * 
 * @execute phase="package"
 * @goal ipk
 */
public class IpkPackaging extends AbstractManagedPackagingMojo
{

  /**
   * A String of the form "name <email-address>" (without quotes).
   * 
   * @parameter default-value="tarent GmbH <ipk-packages@tarent.de>"
   * @required
   */
  private String ipkMaintainer;

  /**
   * The architecture used for the package. If nothing is
   * specified it defaults to <code>all</code>.
   * 
   * @parameter default-value="all"
   */
  private String ipkArchitecture;

  /**
   * Temporary directory that contains the files to be assembled.
   * 
   * @parameter expression="${project.build.directory}/ipk-tmp"
   * @required
   * @readonly
   */
  protected File tempRoot;
  
  public void execute() throws MojoExecutionException
  {
    checkEnvironment();

    String packageName = DebianPackageMap.debianise(artifactId, section);
    String packageVersion = fixVersion(version);

    /*
     * Not using File.separator in this class because we assume being on a UNIXy machine!
     */

    // All files belonging to the package are put into this directory. The layout inside
    // it is done according to what ipkg-build needs.
    File basePkgDir = new File(tempRoot, packageName + "-" + packageVersion);
    
    // The IPK control file (package name, dependencies etc).
    File controlFile = new File(basePkgDir, "CONTROL/control");

    // A file pointing at the source jar (it *MUST* be a jar).
    File srcArtifactFile = new File(outputDirectory.getPath(), finalName + ".jar");

    // The destination file for the project's artifact inside the the package.
    File dstArtifactFile = new File(basePkgDir, "usr/share/java/" + artifactId + ".jar");
    
    // The destination directory for JNI libraries
    File dstJNIDir = new File(basePkgDir, "usr/lib/jni");
    
    // The destination directory for the jars bundled with the project.
    File dstBundledArtifactsDir = new File(basePkgDir, "usr/share/java/" + artifactId);

    // The wrapper script to launch an application (This is unused for libraries).
    File wrapperScriptFile = new File(basePkgDir, "usr/bin/" + artifactId);

    // A set which will be filled with the artifacts which need to be bundled with the
    // application.
    Set bundledArtifacts = new HashSet();
    
    String classpath;
    
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
            classpath = createClasspathLine(l, bundledArtifacts);
            
            // TODO: Handle native library artifacts properly.
            byteAmount += copyArtifacts(l, bundledArtifacts, dstBundledArtifactsDir);

            generateWrapperScript(l, wrapperScriptFile, classpath);
          }
        
        generateControlFile(l,
        		            controlFile,
        		            packageName,
        		            packageVersion,
        		            createDependencyLine(),
        		            ipkArchitecture);

        createPackage(l, basePkgDir);
        
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
	l.info("package system           : Itsy Package Management (.ipk)");
	l.info("type of project          : " + ((mainClass != null) ? "application" : "library"));
	l.info("IPK section              : " + section);
	l.info("bundle all dependencies  : " + ((bundleDependencies) ? "yes" : "no"));
	l.info("JNI libraries            : " + ((jniLibraries == null) ? "none" : String.valueOf(jniLibraries.size())));

	if (mainClass == null)
	  {
		  if (!section.equals("libs"))
			  throw new MojoExecutionException("<section> should be libs if no main class is given.");
		  
		  if (bundleDependencies)
			  throw new MojoExecutionException("bundling dependencies to a library makes no sense.");
	  }
	  else
	  {
        if (section.equals("libs"))
          throw new MojoExecutionException("Set a proper section if main class parameter is set.");
	  }
    
    Utils.exec(new String[] { "which", "ipkg-build" },
               "ipkg-build returned with an error. Check your installation!",
               "ipkg-build is not available on this system. Check your installation!");

    
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
  private void generateControlFile(Log l, File controlFile, String packageName, String packageVersion, String dependencyLine, String architecture)
      throws MojoExecutionException
  {
	ControlFileGenerator cgen = new ControlFileGenerator();
	cgen.setPackageName(packageName);
	cgen.setVersion(packageVersion);
	cgen.setSection(section);
	cgen.setDependencies(dependencyLine);
	cgen.setMaintainer(ipkMaintainer);
	cgen.setArchitecture(architecture);
    cgen.setOE(packageName + "-" + packageVersion);
    
    String url = project.getUrl();
    if (url == null)
    {
    	l.warn("Project has no <url> field. However IPK packages require this. Using a dummy for now.");
    	url = "http://not-yet-set.org/" + packageName;
    }
    cgen.setSource(url);
    cgen.setHomepage(url);
    
    String desc = project.getDescription();
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

  // TODO: This method can be shared with DebianPackaging
  private void generateWrapperScript(Log l, File wrapperScriptFile, String classpathLine)
      throws MojoExecutionException
  {
    l.info("creating wrapper script file: "
           + wrapperScriptFile.getAbsolutePath());
    Utils.createFile(wrapperScriptFile, "wrapper script");

    WrapperScriptGenerator gen = new WrapperScriptGenerator();
    
    gen.setClasspath(classpathLine);
    gen.setMainClass(mainClass);
    gen.setMaxJavaMemory(maxJavaMemory);
    
    // Set to default JNI path on Debian/Ubuntu systems.
    gen.setLibraryPath("/usr/lib/jni");
    
    // Set to default Classmap file on Debian/Ubuntu systems.
    gen.setClassmapFile("/var/lib/gcj-4.1/classmap.db");

    try
      {
        gen.generate(wrapperScriptFile);
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException("IOException while generating wrapper script",
                                         ioe);
      }

    // Make the wrapper script executable.
    Utils.makeExecutable(wrapperScriptFile, "wrapper script");
  }

  private void createPackage(Log l, File basePkgDir) throws MojoExecutionException
  {
    l.info("calling ipkg-build to create binary package");
    
    Utils.exec(new String[] {"ipkg-build",
                             "-o",
                             "root",
                             "-g",
                             "root",
                             basePkgDir.getName(),
                             outputDirectory.getAbsolutePath() },
                tempRoot,
                "'ipkg-build failed.",
                "Error creating the .ipk file.");
  }

  protected String createDependencyLine() throws MojoExecutionException
  {
    return createDependencyLine(DebianPackageMap.getIpkDefaults());
  }

}
