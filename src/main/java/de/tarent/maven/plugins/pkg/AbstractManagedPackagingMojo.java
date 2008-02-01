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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractManagedPackagingMojo
    extends AbstractPackagingMojo
{

  /**
   * Denotes whether all dependencies should be bundled with
   * the application or just the ones that are not packaged.
   * 
   * @parameter default-value="false"
   */
  protected boolean bundleDependencies;
  
  /**
   * The section in Debian into which this package belongs. By default this
   * is "libs".
   * 
   * @parameter expression="libs"
   */
  protected String section;

  /**
   * Denotes a list of dependencies which is added plainly to the
   * "Depends:" line of the control file.</p>
   * 
   * <p>Note that you can even use versioned requirements, e.g. "glibc (>= 2.3.6)".</p>
   * 
   * @parameter
   */
  private List manualDependencies;

  /**
   * Denotes a list of JNI libraries and their locations. The libraries
   * will be copied into the correct location in the package to
   * be installed to their usual location on the target system.
   * 
   * @parameter
   */
  protected List jniLibraries;
  
  /**
   * Resources are key-value pairs whose key denote's a file in the
   * project folder and its location on the target system.
   * 
   * <p>E.g. a pair consisting of the key "src/main/resources/someFile"
   * and the value "/usr/share/pkg/" means that <code>someFile</code>
   * will be installed into the directory <code>/usr/share/pkg</code>.
   * </p>
   * 
   * @parameter
   */
  protected Properties resources;

  /**
   * If the project is an application a main class needs to be specified which
   * is used to generate the wrapper script.
   * 
   * @parameter
   */
  protected String mainClass;

  /**
   * maximum heap space for the jvm
   * 
   * @parameter
   */
  protected String maxJavaMemory;

  /**
   * The java library path set in the start script
   *   
   * @parameter default-value="/usr/lib/jni"
   */
  protected String libraryPath;

  /**
   * Additional system properties specified for the start script.
   * Specified as key-value pairs.
   * 
   * @parameter
   */
  protected Properties systemProperties;


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

  public AbstractManagedPackagingMojo()
  {
    super();
  }

  /**
   * Makes the version string compatible to the system's requirements.
   * TODO: Really check the format and try to fix it and not just remove
   * "-SNAPSHOT"
   * 
   * @param v
   * @return
   */
  protected final String fixVersion(String v)
  {
    int i = v.indexOf("-SNAPSHOT");
    if (i > 0)
      return v.substring(0, i);
  
    return v;
  }

  /**
   * Concatenates two dependency lines. If both parts (prefix and suffix)
   * are non-empty the lines will be joined with a comma in between.
   * 
   * <p>If one of the parts is empty the other will be returned.</p>
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  protected final String joinDependencyLines(String prefix, String suffix)
  {
      return (prefix.length() == 0) ? suffix :
    		 (suffix.length() == 0) ? prefix :
    			                      prefix + ", " + suffix;
  }

  /**
   * Creates the bootclasspath and classpath line from the given dependency artifacts.
   * 
   * @param pm The package map used to resolve the Jar file names.
   * @param bundled A set used to track the bundled jars for later file-size calculations.
   * @param bcp StringBuilder which contains the boot classpath line at the end of the method.
   * @param cp StringBuilder which contains the classpath line at the end of the method.
   * @return
   */
  protected final void createClasspathLine(final Log l, final PackageMap pm,
                                             final Set bundled,
                                             final StringBuilder bcp,
                                             final StringBuilder cp) throws MojoExecutionException
  {
    l.info("resolving dependency artifacts");
    
    Set dependencies = null;
    try
      {
        // Notice only compilation dependencies which are Jars.
        // Shared Libraries ("so") are filtered out because the
        // JNI dependency is solved by the system already.
        AndArtifactFilter andFilter = new AndArtifactFilter();
        andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
        andFilter.add(new TypeArtifactFilter("jar"));
  
        dependencies = findArtifacts(andFilter);
      }
    catch (ArtifactNotFoundException anfe)
      {
        throw new MojoExecutionException("Exception while resolving dependencies",
                                         anfe);
      }
    catch (InvalidDependencyVersionException idve)
      {
        throw new MojoExecutionException("Exception while resolving dependencies",
                                         idve);
      }
    catch (ProjectBuildingException pbe)
      {
        throw new MojoExecutionException("Exception while resolving dependencies",
                                         pbe);
      }
    catch (ArtifactResolutionException are)
      {
        throw new MojoExecutionException("Exception while resolving dependencies",
                                         are);
      }
  
    // Handle the project's own artifact first.
    PackageMap.Entry e = pm.getEntry(project.getArtifactId(), section);
    
    PackageMap.Visitor v = new PackageMap.Visitor()
    {
      public void visit(Artifact artifact, PackageMap.Entry entry)
      {
    	// If all dependencies should be bundled take a short-cut to bundle()
    	// thereby overriding what was configured through property files.
    	if (bundleDependencies)
    	  {
    		bundle(artifact);
    		return;
    	  }
        
          StringBuilder b = (entry.isBootClasspath) ? bcp : cp;
    	  
          Iterator ite = entry.jarFileNames.iterator();
          while (ite.hasNext())
            {
              String fileName = (String) ite.next();
              b.append(fileName);
              b.append(":");
            }
      }
      
      public void bundle(Artifact artifact)
      {
        // Put to artifacts which will be bundled (allows copying and filesize summing later).
    	bundled.add(artifact);
        
        // TODO: Perhaps one want a certain bundled dependency in boot classpath.

    	// Bundled Jar will always live in /usr/share/java/ + artifactId (of the project)
        File file = artifact.getFile();
        if (file != null)
          cp.append(pm.getDefaultJarPath() + artifactId + "/" + file.getName() + ":");
        else
        	l.warn("Cannot put bundled artifact " + artifact.getArtifactId() + " to Classpath.");
      }
      
    };
  
    pm.iterateDependencyArtifacts(dependencies, v, true);

    if (cp.length() > 0)
      cp.deleteCharAt(cp.length() - 1);
    
    if (bcp.length() > 0)
      bcp.deleteCharAt(bcp.length() - 1);
  }

  /**
   * Investigates the project's runtime dependencies and creates a dependency
   * line suitable for the control file from them.
   * 
   * @return
   */
  protected final String createDependencyLine(PackageMap pm) throws MojoExecutionException
  {
    String defaults = pm.getDefaultDependencyLine();
    StringBuffer manualDeps = new StringBuffer();
    if (manualDependencies != null)
      {
    	Iterator ite = manualDependencies.iterator();
    	while (ite.hasNext())
          {
    		String dep = (String) ite.next();
    		
      		manualDeps.append(dep);
            manualDeps.append(", ");
    	  }
        
        manualDeps.delete(manualDeps.length() - 2, manualDeps.length());
      }

    // If all dependencies should be bundled the package will only
    // need the default Java dependencies of the system and the remainder
    // of the method can be skipped.
    if (bundleDependencies)
      return joinDependencyLines(defaults, manualDeps.toString());
    
    Set runtimeDeps = null;
    
    try
      {
    	AndArtifactFilter andFilter = new AndArtifactFilter();
    	andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
    	andFilter.add(new TypeArtifactFilter("jar"));
    	
    	runtimeDeps = findArtifacts(andFilter);
    	
    	andFilter = new AndArtifactFilter();
    	andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
    	andFilter.add(new TypeArtifactFilter("jar"));
    	
    	runtimeDeps.addAll(findArtifacts(andFilter));
      }
    catch (ArtifactNotFoundException anfe)
    {
      throw new MojoExecutionException("Exception while resolving dependencies",
                                       anfe);
    }
    catch (InvalidDependencyVersionException idve)
    {
      throw new MojoExecutionException("Exception while resolving dependencies",
                                       idve);
    }
    catch (ProjectBuildingException pbe)
    {
      throw new MojoExecutionException("Exception while resolving dependencies",
                                       pbe);
    }
    catch (ArtifactResolutionException are)
    {
      throw new MojoExecutionException("Exception while resolving dependencies",
                                       are);
    }
  
    final StringBuilder line = new StringBuilder();
    final Log l = getLog();
  
    // Add default system dependencies for Java packages.
    line.append(defaults);
  
    // Visitor implementation which creates the dependency line.
    PackageMap.Visitor v = new PackageMap.Visitor()
    {
      Set processedDeps = new HashSet();
      
      public void visit(Artifact artifact, PackageMap.Entry entry)
      {
        // Certain Maven Packages have only one package in the target system.
        // If that one was already added we should not add it any more.
        if (processedDeps.contains(entry.packageName))
          return;
        
        if (entry.packageName.length() == 0)
          l.warn("Invalid package name for artifact: " + entry.artifactId);
  
        line.append(", ");
        line.append(entry.packageName);
        
        // Mark as included dependency.
        processedDeps.add(entry.packageName);
      }
      
     public void bundle(Artifact _)
     {
    	 // Nothing to do for bundled artifacts. 
     }
    };
  
    pm.iterateDependencyArtifacts(runtimeDeps, v, true);
  
    return joinDependencyLines(line.toString(), manualDeps.toString());
  }

  /**
   * Copies the files in the map to their destination inside the package
   * base directory.
   * 
   * <p>Interprets the entries in the map as source file and their corresponding
   * destination directory. The destination directory is prepended by the package
   * base directory forming the correct location for the package creation.</p>
   * 
   * <p>All necessary directories are created on the fly.</p>
   * 
   * @param l
   * @param basePkgDir
   * @param resources
   * @param executablePath path for which the resources are marked executable, may be null. Don't put trailing / on the directories.
   * @throws MojoExecutionException
   */
  protected final void copyResources(Log l, File basePkgDir, Map resources, String executablePath) throws MojoExecutionException
  {
    executablePath = executablePath == null ? "" : ":"+executablePath+":";

    if (resources == null || resources.isEmpty())
      return;
    
    Iterator ite = resources.entrySet().iterator();
    while (ite.hasNext())
    {
    	Map.Entry entry = (Map.Entry) ite.next();
        File srcFile = new File(project.getBasedir(), (String) entry.getKey());
    	File dstDir = new File(basePkgDir, (String) entry.getValue());
    	File dstFile = new File(dstDir, srcFile.getName());
    	
    	if (!dstDir.exists())
    	  {
    	    l.info("creating directory for resource: " + dstDir.getAbsolutePath());
    	    Utils.createParentDirs(dstFile, "resource file");
    	  }
  
    	l.info("copying resource: " + srcFile.getAbsolutePath());
    	l.info("destination: " + dstFile.getAbsolutePath());
    	  
    	try
    	  {
    	    FileUtils.copyFile(srcFile, dstFile);
    	  }
    	catch (IOException ioe)
    	  {
    	    throw new MojoExecutionException("IOException while copying resource file.",
    	                                     ioe);
    	  }

        String path = (String)entry.getValue();
        if (path.endsWith("/"))
            path = path.substring(0, path.length()-1);
        if (executablePath.contains(":"+path+":"))
            Utils.makeExecutable(dstFile, dstFile.getName());
    }
    
  }

  protected final void copyJNILibraries(Log l, File dstDir) throws MojoExecutionException
  {
      if (jniLibraries == null || jniLibraries.isEmpty())
    	  return;
      
      Iterator ite = jniLibraries.iterator();
      while (ite.hasNext())
      {
    	  String library = (String) ite.next();
    	  File srcFile = new File(project.getBasedir(), library);
    	  File dstFile = new File(dstDir, srcFile.getName());
  
    	  l.info("copying JNI library: " + srcFile.getAbsolutePath());
    	  l.info("destination: " + dstFile.getAbsolutePath());
    	  
    	  try
    	    {
    	      FileUtils.copyFile(srcFile, dstFile);
    	    }
    	  catch (IOException ioe)
    	    {
    	      throw new MojoExecutionException("IOException while copying JNI library file.",
    	                                       ioe);
    	    }
      }
      
  }

  protected final void copyArtifact(Log l, File src, File dst) throws MojoExecutionException
  {
    l.info("copying artifact: " + src.getAbsolutePath());
    l.info("destination: " + dst.getAbsolutePath());
    Utils.createFile(dst, "destination artifact");
  
    try
      {
        FileUtils.copyFile(src, dst);
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException("IOException while copying artifact file.",
                                         ioe);
      }
  
  }

  /**
   * Creates the temporary and package base directory.
   * 
   * @param l
   * @param basePkgDir
   * @throws MojoExecutionException
   */
  protected final void prepareDirectories(Log l, File tempRoot, File basePkgDir, File jniDir) throws MojoExecutionException
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
  
    l.info("creating package directory: " + basePkgDir.getAbsolutePath());
    if (!basePkgDir.mkdirs())
      throw new MojoExecutionException("Could not create package directory.");
  
    if (jniDir != null && jniLibraries != null && jniLibraries.size() > 0)
      {
        if (!jniDir.mkdirs())
          throw new MojoExecutionException("Could not create JNI directory.");
      }
    	
  }

  /**
   * Configures a wrapper script generator with all kinds of values and
   * creates a wrapper script.
   * 
   * @param l
   * @param wrapperScriptFile
   * @throws MojoExecutionException
   */
  protected void generateWrapperScript(Log l, PackageMap pm, Set bundled, File wrapperScriptFile)
  throws MojoExecutionException
  {
    l.info("creating wrapper script file: "
           + wrapperScriptFile.getAbsolutePath());
    Utils.createFile(wrapperScriptFile, "wrapper script");
  
    WrapperScriptGenerator gen = new WrapperScriptGenerator();
    
    StringBuilder bcp = new StringBuilder();
    StringBuilder cp = new StringBuilder();
    createClasspathLine(l, pm, bundled, bcp, cp);
    
    gen.setBootClasspath(bcp.toString());
    gen.setClasspath(cp.toString());
    gen.setMainClass(mainClass);
    gen.setMaxJavaMemory(maxJavaMemory);
    gen.setLibraryPath(pm.getDefaultJNIPath());
    gen.setProperties(systemProperties);
    
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
  
}
