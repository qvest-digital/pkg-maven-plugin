

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

import de.tarent.maven.plugins.pkg.generator.WrapperScriptGenerator;
import de.tarent.maven.plugins.pkg.map.Entry;
import de.tarent.maven.plugins.pkg.map.PackageMap;
import de.tarent.maven.plugins.pkg.map.Visitor;
import de.tarent.maven.plugins.pkg.packager.DebPackager;
import de.tarent.maven.plugins.pkg.packager.IpkPackager;
import de.tarent.maven.plugins.pkg.packager.IzPackPackager;
import de.tarent.maven.plugins.pkg.packager.Packager;

/**
 * Creates a package file for the project and the given distribution.
 * 
 * @execute phase="package"
 * @goal pkg
 */
public class Packaging
    extends AbstractPackagingMojo
{

  /**
   * So umarbeiten, dass:
   * 
   * - jedes sinnvolle property bekommt setter und getter
   * - der getter enthält einen vorgegebenen sinnvollen erzeugercode,
   *   der zur ausführung kommt, wenn der wert nicht gesetzt ist.
   * - benutzer von Packaging.Helper müssen die Werte fest nach ihren eigenen Regeln setzen
   * - die Aktions-Methoden (copy<...> arbeiten mit so wenig wie möglich parametern)
   *  und beziehen alles aus den übrigen methoden
   *  
   *  TODO: Explain and document the design idea of this class. 
   * 
   * @author Robert Schuster (robert.schuster@tarent.de)
   *
   */
  public class Helper
  {
    String aotPackageName;

    /**
     * The base directory for the gcj package.
     */
    File aotPkgDir;

    /**
     * All files belonging to the package are put into this directory. For deb
     * packaging the layout inside is done according to `man dpkg-deb`.
     */
    File basePkgDir;

    /**
     * The destination file for the project's artifact inside the the package at
     * construction time (equals ${basePkgDir}/${targetArtifactFile}).
     */
    File dstArtifactFile;
    
    File dstBundledArtifactsDir;
    
    /**
     * The destination directory for JNI libraries at package building time
     *(e.g. starts with "${project.outputdir}/")
     */
    File dstJNIDir;

    File dstStarterDir;

    String packageName;

    String packageVersion;

    /**
     * A file pointing at the source jar (it *MUST* be a jar).
     */
    File srcArtifactFile;

    /**
     * Location of the project's artifact on the target system (needed for the
     * classpath construction). (e.g. /usr/share/java/app-2.0-SNAPSHOT.jar)
     */
    File targetArtifactFile;
    
    /**
     * Location of the project's dependency artifacts on the target system (needed
     * for classpath construction.).
     * 
     * <p>If the path contains a variable that is to be replaced by an installer
     * it must not be used in actual file operations! To prevent this from happening
     * provide explicit value for all properties which use {@link #getTargetArtifactFile()}</p>
     * 
     * (e.g. ${INSTALL_DIR}/libs)
     */
    File targetJarDir;

    /**
     * Location of the JNI libraries on the target device (e.g. /usr/lib/jni).
     */
    File targetJNIDir;

    File targetStarterDir;
    
    File tempRoot;
    
    File windowsWrapperScriptFile;
    
    File wrapperScriptFile;
    
    Helper()
    {
    }

    /**
     * Copies the given set of artifacts to the location specified
     * by {@link #getDstBundledArtifactsDir()}. 
     * 
     * @param artifacts
     * @param dst
     * @return
     * @throws MojoExecutionException
     */
    public long copyArtifacts(Set artifacts)
        throws MojoExecutionException
    {
      return Packaging.this.copyArtifacts(getLog(), artifacts, getDstBundledArtifactsDir());
    }
    
    /**
     * Copies the project's declared JNI libraries into the location specified by
     * {@link #getDstJNIDir()}.
     * 
     * @throws MojoExecutionException
     */
    public void copyJNILibraries() throws MojoExecutionException
    {
      Packaging.this.copyJNILibraries(getLog(), dc.jniLibraries, getDstJNIDir());
    }

    /**
     * Copies the project's artifact file possibly renaming it.
     * 
     * <p>For the destination the value of the property
     * <code<dstArtifactFile</code> is used.</p> 
     * 
     * @throws MojoExecutionException
     */
    public void copyProjectArtifact() throws MojoExecutionException
    {
      Utils.copyProjectArtifact(getLog(), getSrcArtifactFile(), getDstArtifactFile());
    }

    /**
     * Creates a classpath line that consists of all the project' artifacts as well as
     * the project's own artifact.
     * 
     * <p>The filename of the project's own artifact is taken from the result of
     * {@link #getTargetArtifactFile()}.</p>
     * 
     * <p>The method returns a set of artifact instance which will be bundled
     * with the package.</p>
     * 
     * @param bcp
     * @param cp
     * @throws MojoExecutionException
     */
    public Set createClasspathLine(StringBuilder bcp, StringBuilder cp)
        throws MojoExecutionException
    {
      return Packaging.this.createClasspathLine(getLog(),
                                                getTargetJarDir(),
                                                bcp,
                                                ":",
                                                cp,
                                                (dc.isAdvancedStarter() ? "\n" : ":"),
                                                getTargetArtifactFile());
    }

    public String createDependencyLine() throws MojoExecutionException
    {
      return Packaging.this.createDependencyLine();
    }
    
    public void generateWrapperScript(Set bundledArtifacts, String bcp, String cp, boolean windows)
    throws MojoExecutionException
    {
      Log l = getLog();
      WrapperScriptGenerator gen = new WrapperScriptGenerator();
      gen.setMaxJavaMemory(dc.maxJavaMemory);
      
      if (getTargetJNIDir() != null)
        gen.setLibraryPath(getTargetJNIDir().toString());
      
      gen.setProperties(dc.systemProperties);

      // Set to default Classmap file on Debian/Ubuntu systems.
      // TODO: make this configurable
      if (dc.isAotCompile())
        gen.setClassmapFile("/var/lib/gcj-4.1/classmap.db");
      
      if (dc.isAdvancedStarter())
        {
          l.info("setting up advanced starter");
          Utils.setupStarter(l, dc.getMainClass(), getDstStarterDir(), cp);
  
          // Sets main class and classpath for the wrapper script.
          gen.setMainClass("_Starter");
          gen.setClasspath(getTargetStarterDir().toString());
        }
      else
        {
        l.info("using traditional starter");
        gen.setMainClass(dc.getMainClass());

        // All Jars have to reside inside the libraryRoot.
        gen.setClasspath(cp);
        }

      Utils.createFile(getWrapperScriptFile(), "wrapper script");

      try
        {
          gen.generate(getWrapperScriptFile());
        }
      catch (IOException ioe)
        {
          throw new MojoExecutionException(
                                           "IOException while generating wrapper script",
                                           ioe);
        }
      
      if (windows)
        {
          Utils.createFile(getWindowsWrapperScriptFile(), "windows batch");

          try
            {
              gen.generate(getWindowsWrapperScriptFile());
            }
          catch (IOException ioe)
            {
              throw new MojoExecutionException("IOException while generating windows batch file",
                                               ioe);
            }
        }

      // Make the wrapper script executable.
      Utils.makeExecutable(getWrapperScriptFile(), "wrapper script");
      
    }
    
    public String getAotPackageName()
    {
      if (aotPackageName == null)
        aotPackageName = Utils.gcjise(artifactId, dc.getSection(),
                                      pm.isDebianNaming());

      
      return aotPackageName;
    }
    
    public File getAotPkgDir()
    {
      if (aotPkgDir == null)
        aotPkgDir = new File(getTempRoot(), aotPackageName + "-" + getPackageVersion());

      return aotPkgDir;
    }
    
    public String getArtifactId()
    {
      return artifactId;
    }

    public File getAuxFileSrcDir()
    {
      return new File(project.getBasedir(), dc.getAuxFileSrcDir());
    }

    public File getBasePkgDir()
    {
      if (basePkgDir == null)
        basePkgDir = new File(getTempRoot(), getPackageName() + "-" + getPackageVersion());
      
      return basePkgDir;
    }

    public File getDstArtifactFile()
    {
      if (dstArtifactFile == null)
        dstArtifactFile = new File(getBasePkgDir(),
                                   getTargetArtifactFile().toString());

      return dstArtifactFile;
    }

    public File getDstBundledArtifactsDir()
    {
      if (dstBundledArtifactsDir == null)
        dstBundledArtifactsDir = new File(basePkgDir, getTargetJarDir().toString());
      
      return dstBundledArtifactsDir;
    }

    public File getDstJNIDir()
    {
      if (dstJNIDir == null)
        dstJNIDir = new File(getBasePkgDir(), getTargetJNIDir().toString());

      return dstJNIDir;
    }

    public File getDstStarterDir()
    {
      if (dstStarterDir == null)
        dstStarterDir = new File(getBasePkgDir(), getTargetStarterDir().toString());
      
      return dstStarterDir;
    }
    
    public File getIzPackSrcDir()
    {
      return new File(project.getBasedir(), dc.izPackSrcDir);
    }

    public String getJavaExec()
    {
      return javaExec;
    }

    public File getOutputDirectory()
    {
      return outputDirectory;
    }

    public String getPackageName()
    {
      if (packageName == null)
        packageName = Utils.createPackageName(artifactId, dc.getSection(), pm.isDebianNaming());
      
      return packageName;
    }

    public String getPackageVersion()
    {
      if (packageVersion == null)
        packageVersion = fixVersion(version) + "-0" + dc.chosenDistro;

      return packageVersion;
    }

    public String getProjectDescription()
    {
      return project.getDescription();
    }

    public String getProjectUrl()
    {
      return project.getUrl();
    }

    public File getSrcArtifactFile()
    {
      if (srcArtifactFile == null)
        srcArtifactFile = new File(outputDirectory.getPath(), finalName + ".jar");

      return srcArtifactFile;
    }

    public File getTargetArtifactFile()
    {
      if (targetArtifactFile == null)
          targetArtifactFile = new File(getTargetJarDir(), artifactId + ".jar");

      return targetArtifactFile;
    }
    
    public File getTargetJarDir()
    {
      if (targetJarDir == null)
        targetJarDir = new File(pm.getDefaultJarPath(),  artifactId);
      
      return targetJarDir;
    }
    
    public File getTargetJNIDir()
    {
      if (targetJNIDir == null)
        targetJNIDir = new File(pm.getDefaultJNIPath());

      return targetJNIDir;
    }
    
    public File getTargetStarterDir()
    {
      if (targetStarterDir == null)
        targetStarterDir = new File(getTargetJarDir(), "_starter");
      
      return targetStarterDir;
    }

    public File getTempRoot()
    {
      if (tempRoot == null)
        tempRoot = new File(buildDir, pm.getPackaging() + "-tmp");

      return tempRoot;
    }
    
    public File getWindowsWrapperScriptFile()
    {
      if (windowsWrapperScriptFile == null)
        windowsWrapperScriptFile = new File(getWrapperScriptFile().getAbsolutePath() + ".bat");
      
      return windowsWrapperScriptFile;
    }

    public File getWrapperScriptFile()
    {
      if (wrapperScriptFile == null)
        // Use the provided wrapper script name or the default.
        wrapperScriptFile = new File(getBasePkgDir(), pm.getDefaultBinPath() + "/"
                                                 + (dc.wrapperScriptName != null ? dc.wrapperScriptName : artifactId));

      return wrapperScriptFile;
    }

    public void prepareAotDirectories() throws MojoExecutionException
    {
      prepareDirectories(getLog(), tempRoot, aotPkgDir, null);
    }

    public void prepareInitialDirectories() throws MojoExecutionException
    {
      prepareDirectories(getLog(), tempRoot, basePkgDir, dstJNIDir);
    }

    public void setAotPackageName(String aotPackageName)
    {
      this.aotPackageName = aotPackageName;
    }

    public void setAotPkgDir(File aotPkgDir)
    {
      this.aotPkgDir = aotPkgDir;
    }
    
    public void setBasePkgDir(File basePkgDir)
    {
      this.basePkgDir = basePkgDir;
    }

    public void setDstArtifactFile(File dstArtifactFile)
    {
      this.dstArtifactFile = dstArtifactFile;
    }

    public void setDstBundledArtifactsDir(File dstBundledArtifactsDir)
    {
      this.dstBundledArtifactsDir = dstBundledArtifactsDir;
    }

    public void setDstJNIDir(File dstJNIDir)
    {
      this.dstJNIDir = dstJNIDir;
    }

    public void setDstStarterDir(File dstStarterDir)
    {
      this.dstStarterDir = dstStarterDir;
    }

    public void setPackageName(String packageName)
    {
      this.packageName = packageName;
    }

    public void setPackageVersion(String packageVersion)
    {
      this.packageVersion = packageVersion;
    }

    public void setTargetArtifactFile(File targetArtifactFile)
    {
      this.targetArtifactFile = targetArtifactFile;
    }

    public void setTargetJarDir(File targetJarDir)
    {
      this.targetJarDir = targetJarDir;
    }

    public void setTargetJNIDir(File targetJNIDir)
    {
      this.targetJNIDir = targetJNIDir;
    }

    public void setTargetStarterDir(File targetStarterDir)
    {
      this.targetStarterDir = targetStarterDir;
    }

    public void setTempRoot(File tempRoot)
    {
      this.tempRoot = tempRoot;
    }

    public void setWindowsWrapperScriptFile(File windowsWrapperScriptFile)
    {
      this.windowsWrapperScriptFile = windowsWrapperScriptFile;
    }

    public void setWrapperScriptFile(File wrapperScriptFile)
    {
      this.wrapperScriptFile = wrapperScriptFile;
    }
  }

  private DistroConfiguration dc;

  /**
   * @parameter
   * @required
   */
  protected DistroConfiguration defaults;

  /**
   * @parameter
   */
  protected List distroConfigurations;

  private PackageMap pm;

  /**
   * Validates arguments and test tools.
   * 
   * @throws MojoExecutionException
   */
  void checkEnvironment(Log l) throws MojoExecutionException
  {
    l.info("distribution             : " + dc.chosenDistro);
    l.info("package system           : " + pm.getPackaging());
    l.info("default package map      : " + (defaultPackageMapURL == null ? "built-in" : defaultPackageMapURL.toString()));
    l.info("auxiliary package map    : " + (auxPackageMapURL == null ? "no" : auxPackageMapURL.toString()));
    l.info("type of project          : " + ((dc.getMainClass() != null) ? "application" : "library"));
    l.info("section                  : " + dc.getSection());
    l.info("bundle all dependencies  : " + ((dc.isBundleAll()) ? "yes" : "no"));
    l.info("ahead of time compilation: " + ((dc.isAotCompile()) ? "yes" : "no"));
    l.info("JNI libraries            : " + ((dc.getJniLibraries() == null) ? "none" : String.valueOf(dc.getJniLibraries().size())));

    if (dc.chosenDistro == null)
      throw new MojoExecutionException("No distribution configured!");

    if (dc.isAotCompile())
      {
        l.info("aot compiler             : " + dc.getGcjExec());
        l.info("aot classmap generator   : " + dc.getGcjDbToolExec());
      }

    if (dc.getMainClass() == null)
      {
        if (! "libs".equals(dc.getSection()))
          throw new MojoExecutionException(
                                           "section has to be 'libs' if no main class is given.");

        if (dc.isBundleAll())
          throw new MojoExecutionException(
                                           "Bundling dependencies to a library makes no sense.");
      }
    else
      {
        if ("libs".equals(dc.getSection()))
          throw new MojoExecutionException(
                                           "Set a proper section if main class parameter is set.");
      }

    if (dc.isAotCompile())
      {
        AotCompileUtils.setGcjExecutable(dc.getGcjExec());
        AotCompileUtils.setGcjDbToolExecutable(dc.getGcjDbToolExec());

        AotCompileUtils.checkToolAvailability();
      }
  }

  final void copyJNILibraries(Log l, List jniLibraries, File dstDir)
      throws MojoExecutionException
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
            throw new MojoExecutionException(
                                             "IOException while copying JNI library file.",
                                             ioe);
          }
      }

  }

  /**
   * Creates the bootclasspath and classpath line from the project's dependencies
   * and returns the artifacts which will be bundled with the package.
   * 
   * @param pm The package map used to resolve the Jar file names.
   * @param bundled A set used to track the bundled jars for later file-size
   *          calculations.
   * @param bcp StringBuilder which contains the boot classpath line at the end
   *          of the method.
   * @param cp StringBuilder which contains the classpath line at the end of the
   *          method.
   * @return
   */
  protected final Set createClasspathLine(final Log l,
                                          final File targetJarPath,
                                           final StringBuilder bcp,
                                           final String bcpDelimiter,
                                           final StringBuilder cp,
                                           final String cpDelimiter,
                                           File targetArtifactFile)
      throws MojoExecutionException
  {
    final Set bundled = new HashSet();
    
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
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         anfe);
      }
    catch (InvalidDependencyVersionException idve)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         idve);
      }
    catch (ProjectBuildingException pbe)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         pbe);
      }
    catch (ArtifactResolutionException are)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         are);
      }

    Visitor v = new Visitor()
    {
      public void bundle(Artifact artifact)
      {
        // Put to artifacts which will be bundled (allows copying and filesize
        // summing later).
        bundled.add(artifact);

        // TODO: Perhaps one want a certain bundled dependency in boot
        // classpath.

        // Bundled Jars will always live in targetJarPath 
        File file = artifact.getFile();
        if (file != null)
          cp.append(targetJarPath.toString() + "/" + file.getName()
                    + cpDelimiter);
        else
          l.warn("Cannot put bundled artifact " + artifact.getArtifactId()
                 + " to Classpath.");
      }

      public void visit(Artifact artifact, Entry entry)
      {
        // If all dependencies should be bundled take a short-cut to bundle()
        // thereby overriding what was configured through property files.
        if (dc.isBundleAll())
          {
            bundle(artifact);
            return;
          }

        StringBuilder b = (entry.isBootClasspath) ? bcp : cp;
        String delimiter = (entry.isBootClasspath) ? bcpDelimiter : cpDelimiter;

        Iterator ite = entry.jarFileNames.iterator();
        while (ite.hasNext())
          {
            String fileName = (String) ite.next();
            
            // Prepend default Jar path if file is not absolute.
            if (fileName.charAt(0) != '/')
              {
                b.append(pm.getDefaultJarPath());
                b.append("/");
              }
            
            b.append(fileName);
            b.append(delimiter);
          }
      }

    };

    pm.iterateDependencyArtifacts(l, dependencies, v, true);

    // Add the project's own artifact at last. This way we can
    // save the deletion of the colon added in the loop.
    cp.append(targetArtifactFile.toString());

    if (bcp.length() > 0)
      bcp.delete(bcp.length() - bcpDelimiter.length(), bcp.length());
    
    return bundled;
  }

  /**
   * Investigates the project's runtime dependencies and creates a dependency
   * line suitable for the control file from them.
   * 
   * @return
   */
  protected final String createDependencyLine() throws MojoExecutionException
  {
    String defaults = pm.getDefaultDependencyLine();
    StringBuffer manualDeps = new StringBuffer();
    Iterator ite = dc.manualDependencies.iterator();
    while (ite.hasNext())
      {
        String dep = (String) ite.next();

        manualDeps.append(dep);
        manualDeps.append(", ");
      }

    if (manualDeps.length() >= 2)
      manualDeps.delete(manualDeps.length() - 2, manualDeps.length());

    // If all dependencies should be bundled the package will only
    // need the default Java dependencies of the system and the remainder
    // of the method can be skipped.
    if (dc.isBundleAll())
      return Utils.joinDependencyLines(defaults, manualDeps.toString());

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
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         anfe);
      }
    catch (InvalidDependencyVersionException idve)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         idve);
      }
    catch (ProjectBuildingException pbe)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         pbe);
      }
    catch (ArtifactResolutionException are)
      {
        throw new MojoExecutionException(
                                         "Exception while resolving dependencies",
                                         are);
      }

    final StringBuilder line = new StringBuilder();
    final Log l = getLog();

    // Add default system dependencies for Java packages.
    line.append(defaults);

    // Visitor implementation which creates the dependency line.
    Visitor v = new Visitor()
    {
      Set processedDeps = new HashSet();

      public void bundle(Artifact _)
      {
        // Nothing to do for bundled artifacts.
      }

      public void visit(Artifact artifact, Entry entry)
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
    };

    pm.iterateDependencyArtifacts(l, runtimeDeps, v, true);

    return Utils.joinDependencyLines(line.toString(), manualDeps.toString());
  }

  public void execute() throws MojoExecutionException, MojoFailureException
  {
    String d = (distro != null) ? distro : defaultDistro;

    // Generate merged distro configuration.
    dc = getMergedConfiguration(d);
    dc.chosenDistro = d;
    
    // Retrieve package map for chosen distro.
    pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
                        dc.bundleDependencies);

    Helper ph = new Helper();

    String packaging = pm.getPackaging();
    if (packaging == null)
      throw new MojoExecutionException("Package maps document set no packaging for distro: "
                                           + dc.chosenDistro);

    // Create packager according to the chosen packaging type.
    Packager packager;
    if ("deb".equals(packaging))
      packager = new DebPackager();
    else if ("ipk".equals(packaging))
      packager = new IpkPackager();
    else if ("izpack".equals(packaging))
      packager = new IzPackPackager();
    else
      throw new MojoExecutionException("Unsupported packaging type: "
                                       + packaging);

    checkEnvironment(getLog());

    packager.checkEnvironment(getLog(), dc);

    packager.execute(getLog(), ph, dc, pm);
  }
 
  /**
   * Takes the default configuration and the custom one into account and creates
   * a merged one.
   * 
   * @param distro
   * @return
   */
  private DistroConfiguration getMergedConfiguration(String distro)
      throws MojoExecutionException
  {
    // If no special config exist use the plain default.
    if (distroConfigurations == null || distroConfigurations.size() == 0)
      return new DistroConfiguration().merge(defaults);

    Iterator ite = distroConfigurations.iterator();
    while (ite.hasNext())
      {
        DistroConfiguration dc = (DistroConfiguration) ite.next();

        // Checks whether this distroconfiguration supports
        // the wanted distro.
        if (dc.distros.contains(distro))
          {
            // Stores the chosen distro in the configuration for later use.
            dc.chosenDistro = distro;
            
            // Returns a configuration that is merged with
            // the default configuration-
            return dc.merge(defaults);
          }
      }

    // No special config for chosen distro available.
    return new DistroConfiguration().merge(defaults);
  }

  /**
   * Creates the temporary and package base directory.
   * 
   * @param l
   * @param basePkgDir
   * @throws MojoExecutionException
   */
  final void prepareDirectories(Log l, File tempRoot, File basePkgDir,
                                File jniDir) throws MojoExecutionException
  {
    l.info("creating temporary directory: " + tempRoot.getAbsolutePath());

    if (! tempRoot.exists() && ! tempRoot.mkdirs())
      throw new MojoExecutionException("Could not create temporary directory.");

    l.info("cleaning the temporary directory");
    try
      {
        FileUtils.cleanDirectory(tempRoot);
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException(
                                         "Exception while cleaning temporary directory.",
                                         ioe);
      }

    l.info("creating package directory: " + basePkgDir.getAbsolutePath());
    if (! basePkgDir.mkdirs())
      throw new MojoExecutionException("Could not create package directory.");

    if (jniDir != null && dc.jniLibraries != null && dc.jniLibraries.size() > 0)
      {
        if (! jniDir.mkdirs())
          throw new MojoExecutionException("Could not create JNI directory.");
      }

  }
}
