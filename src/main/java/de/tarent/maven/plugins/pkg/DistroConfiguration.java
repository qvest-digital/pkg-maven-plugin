

package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A <code>DistroConfiguration</code> provides the properties
 * to configure the packaging for a particular target system.
 * 
 * <p>Except for the boolean properties every field can be accessed
 * directly. The boolean properties are using <code>Boolean</code>
 * to allow them to be <code>null</code> which means 'not set' and
 * is an important state for the merging of two
 * <code>DistroConfiguration</code> instances.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
public class DistroConfiguration
{
  /**
   * Denotes the distribution id this configuration is for. There should
   * be only one configuration per distribution.
   */
  String distro;

  /**
   * Denotes wether the packager should invoke ahead of time compilation
   * (if it supports this).
   * 
   * <p>Default is <code>null</code>, after merging it is <code>false</code>
   * or the parent's value.</p>
   */
  Boolean aotCompile;

  /**
   * Denotes the architecure string to be used. This is only effective for
   * packagers supporting this feature (= ipk, deb).
   * 
   * <p>Default is <code>null</code>, after merging it is <code>all</code>
   * or the parent's value.</p>
   */
  String architecture;

  /**
   * Denotes whether the packager should use a special starter class to run
   * the application which allows working around platform limitations as
   * fixed command-line length. 
   * 
   * <p>Default is <code>null</code>, after merging it is <code>false</code>
   * or the parent's value.</p>
   */
  Boolean advancedStarter;

  /**
   * Denotes whether the packager should bundle every dependency regardless
   * of whether a particular item is available from the system's native package
   * management or not. This can be used to work around problems with those
   * packages.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>false</code>
   * or the parent's value.</p>
   */
  Boolean bundleAll;

  /**
   * Denotes the directory in which the packager looks for auxiliary files to
   * copy into the package.
   * 
   * <p>By using this property one can define a common filename set which has
   * to be copied but works on different files since the <code>auxFileSrcDir</code>
   * property can be changed on a per distribution basis.</p>
   * 
   * <p>Note: The path must be relative to the project's base dir.</p>
   * 
   * <p>Default is <code>null</code>, after merging it is <code>src/main/auxfiles</code>
   * or the parent's value.</p>
   */
  String auxFileSrcDir;

  /**
   * Denotes the name of the gcj-dbtool executable. This allows the use of e.g. "gcj-dbtool-4.2"
   * or "gcj-dbtool-4.3" depending on the targeted distribution.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>gcj-dbtool</code>
   * or the parent's value.</p>
   */
  String gcjDbToolExec;

  /**
   * Denotes the name of the gcj executable. This allows the use of e.g. "gcj-4.2"
   * or "gcj-4.3" depending on the targeted distribution.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>gcj</code>
   * or the parent's value.</p>
   */
  String gcjExec;

  /**
   * Denotes a set of dependencies (in Maven's artifact id naming) that should
   * be bundled with the application regardless of their existence in the target system's
   * native package management.
   * 
   * <p>Default is <code>null</code>, after merging it is an empty set
   * or the parent's value.</p>
   */
  Set bundleDependencies;

  /**
   * Denotes a list of native libraries. These are copied to their respective destination
   * suitable for the chosen target system. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List jniLibraries;

  /**
   * Denotes the <code>java.library.path</code> value of the application. In case
   * of IzPack packaging do not forget to use the "$install_path" variable. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty string
   * or the parent's value.</p>
   */
  String jniLibraryPath;

  /**
   * Denotes the applications' main class. It can be different per distribution, which
   * might be handy for different start screens or workarounds. 
   * 
   * <p>Default is <code>null</code>, after merging it is <code>null</code>
   * or the parent's value.</p>
   */
  String mainClass;

  /**
   * Denotes the value of the maintainer field in common packaging systems. It is 
   * basically an email address. 
   * 
   * <p>Default is <code>null</code>, after merging it is <code>null</code>
   * or the parent's value.</p>
   */
  String maintainer;

  /**
   * Denotes a list of dependency strings which should be added to the automatically
   * generated ones. This allows to specify dependencies which Maven does not know
   * about. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List manualDependencies;
  
  /**
   * Denotes a list of {@link AuxFile} instances specifying additional files
   * that need to be added to the package. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List auxFiles;

  /**
   * Denotes the value of the "-Xmx" argument. 
   * 
   * <p>Default is <code>null</code>, after merging it is <code>null</code>
   * or the parent's value.</p>
   */
  String maxJavaMemory;

  /**
   * Denotes the value of the section property supported by packaging systems.   
   * 
   * <p>Default is <code>null</code>, after merging it is "libs"
   * or the parent's value.</p>
   */
  String section;

  /**
   * Denotes a bunch of system properties keys and their values which are added
   * to the starter script and thus provided to the application.  
   * 
   * <p>Default is <code>null</code>, after merging it is an empty <code>Properties</code>
   * instance or the parent's value.</p>
   */
  Properties systemProperties;
  
  /**
   * Denotes the name of the wrapper script that is used to run the application. This property
   * is optional and will default to the <code>artifactId</code> is the Maven project. For
   * Windows targets ".bat" is appended to this name.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>null</code>
   * or the parent's value.</p>
   */
  String wrapperScriptName;
  
  /**
   * Denotes the directory in which the packager looks for the izpack configuration and
   * files. 
   * 
   * <p>Note: The path must be relative to the project's base dir.</p>
   * 
   * <p>Default is <code>null</code>, after merging it is <code>src/main/izpack</code>
   * or the parent's value.</p>
   */
  String izPackSrcDir;
  
  /**
   * Denotes the name of the IzPack descriptor file.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>installer.xml</code>
   * or the parent's value.</p>
   */
  String izPackInstallerXml;

  public DistroConfiguration()
  {
    // For instantiation.
  }

  public String getArchitecture()
  {
    return architecture;
  }

  public String getDistro()
  {
    return distro;
  }

  public String getGcjDbToolExec()
  {
    return gcjDbToolExec;
  }

  public String getGcjExec()
  {
    return gcjExec;
  }

  public List getJniLibraries()
  {
    return jniLibraries;
  }

  public String getJniLibraryPath()
  {
    return jniLibraryPath;
  }

  public String getMainClass()
  {
    return mainClass;
  }

  public String getMaintainer()
  {
    return maintainer;
  }

  public List getManualDependencies()
  {
    return manualDependencies;
  }

  public String getMaxJavaMemory()
  {
    return maxJavaMemory;
  }

  public String getSection()
  {
    return section;
  }

  public Properties getSystemProperties()
  {
    return systemProperties;
  }

  public boolean isAotCompile()
  {
    return aotCompile.booleanValue();
  }

  public boolean isBundleAll()
  {
    return bundleAll.booleanValue();
  }

  public void setAotCompile(boolean aotCompile)
  {
    this.aotCompile = Boolean.valueOf(aotCompile);
  }

  public void setArchitecture(String architecture)
  {
    this.architecture = architecture;
  }

  public void setBundleAll(boolean bundleAll)
  {
    this.bundleAll = Boolean.valueOf(bundleAll);
  }

  public void setDistro(String distro)
  {
    this.distro = distro;
  }

  public void setGcjDbToolExec(String gcjDbToolExec)
  {
    this.gcjDbToolExec = gcjDbToolExec;
  }

  public void setGcjExec(String gcjExec)
  {
    this.gcjExec = gcjExec;
  }

  public void setJniLibraries(List jniLibraries)
  {
    this.jniLibraries = jniLibraries;
  }

  public void setJniLibraryPath(String jniLibraryPath)
  {
    this.jniLibraryPath = jniLibraryPath;
  }

  public void setMainClass(String mainClass)
  {
    this.mainClass = mainClass;
  }

  public void setMaintainer(String maintainer)
  {
    this.maintainer = maintainer;
  }

  public void setManualDependencies(List manualDependencies)
  {
    this.manualDependencies = manualDependencies;
  }

  public void setMaxJavaMemory(String maxJavaMemory)
  {
    this.maxJavaMemory = maxJavaMemory;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public void setSystemProperties(Properties systemProperties)
  {
    this.systemProperties = systemProperties;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("distro: " + distro + "\n");
    sb.append("aotCompile: " + aotCompile + "\n");
    sb.append("architecture: " + architecture + "\n");
    sb.append("bundleAll: " + bundleAll + "\n");
    sb.append("advancedStarter: " + advancedStarter + "\n");

    sb.append("gcjDbToolExec: " + gcjDbToolExec + "\n");
    sb.append("gcjExec: " + gcjExec + "\n");
    sb.append("jniLibraries: " + jniLibraries + "\n");
    sb.append("jniLibraryPath: " + jniLibraryPath + "\n");
    sb.append("izPackSrcDir: " + izPackSrcDir + "\n");
    sb.append("izPackInstallerXml: " + izPackInstallerXml + "\n");

    sb.append("mainClass: " + mainClass + "\n");
    sb.append("maintainer: " + maintainer + "\n");

    sb.append("maxJavaMemory: " + maxJavaMemory + "\n");
    sb.append("section: " + section + "\n");

    sb.append("manualDependencies:");
    if (manualDependencies != null)
      {
        Iterator ite = manualDependencies.iterator();
        while (ite.hasNext())
          sb.append(ite.next());
        sb.append("\n");
      }

    sb.append("bundleDependencies:");
    if (bundleDependencies != null)
      {
        Iterator ite = bundleDependencies.iterator();
        while (ite.hasNext())
          sb.append(ite.next());
        sb.append("\n");
      }

    sb.append("auxFiles:");
    if (auxFiles != null)
      {
        Iterator ite = auxFiles.iterator();
        while (ite.hasNext())
          {
            AuxFile af = (AuxFile) ite.next();
            sb.append(af.from + " -> " + af.to);
            sb.append("\n");
          }
      }

    sb.append("systemProperties:\n");
    sb.append(systemProperties);
    sb.append("\n");

    return sb.toString();
  }

  public Set getBundleDependencies()
  {
    return bundleDependencies;
  }

  public void setBundleDependencies(Set bundleDependencies)
  {
    this.bundleDependencies = bundleDependencies;
  }

  /**
   * Sets all unset properties, either to the values of the parent or to a (hard-coded) 
   * default value if the property is not set in the parent.
   * 
   * <p>Using this method the packaging plugin can generate a merge of the default
   * and a distro-specific configuration.</p>
   * 
   * @param parent
   * @return
   */
  DistroConfiguration merge(DistroConfiguration parent)
  {
    aotCompile = (Boolean) merge(aotCompile, parent.aotCompile, Boolean.FALSE);
    bundleAll = (Boolean) merge(bundleAll, parent.bundleAll, Boolean.FALSE);
    advancedStarter = (Boolean) merge(advancedStarter, parent.advancedStarter, Boolean.FALSE);

    architecture = (String) merge(architecture, parent.architecture, "all");
    gcjDbToolExec = (String) merge(gcjDbToolExec, parent.gcjDbToolExec,
                                      "gcj");
    gcjExec = (String) merge(gcjExec, parent.gcjExec, "gcj-dbtool");

    jniLibraryPath = (String) merge(jniLibraryPath, parent.jniLibraryPath,
                                    "/usr/lib/jni");
    
    mainClass = (String) merge(mainClass, parent.mainClass, null);
    wrapperScriptName = (String) merge(wrapperScriptName, parent.wrapperScriptName, null);
    maintainer = (String) merge(maintainer, parent.maintainer, null);
    maxJavaMemory = (String) merge(maxJavaMemory, parent.maxJavaMemory, null);
    section = (String) merge(section, parent.section, "libs");
    izPackInstallerXml = (String) merge(izPackInstallerXml, parent.izPackInstallerXml, "installer.xml");
    izPackSrcDir = (String) merge(izPackSrcDir, parent.izPackSrcDir, "src/main/izpack");
    auxFileSrcDir = (String) merge(auxFileSrcDir, parent.auxFileSrcDir, "src/main/auxfiles");

    bundleDependencies = (Set) merge(bundleDependencies,
                                        parent.bundleDependencies,
                                        new HashSet());

    auxFiles = (List) merge(auxFiles, parent.auxFiles,
                                new ArrayList());

    jniLibraries = (List) merge(jniLibraries, parent.jniLibraries,
                                   new ArrayList());
    manualDependencies = (List) merge(jniLibraries, parent.jniLibraries,
                                         new ArrayList());

    systemProperties = (Properties) merge(systemProperties,
                                             parent.systemProperties,
                                             new Properties());

    return this;
  }

  /**
   * If child != null, take child (overriden parent), else if parent != null,
   * take parent (overriden default), else take default.
   * 
   * @param child
   * @param parent
   * @param def
   * @return
   */
  private static Object merge(Object child, Object parent, Object def)
  {
    return (child != null) ? child : (parent != null ? parent : def);
  }

  public String getWrapperScriptName()
  {
    return wrapperScriptName;
  }

  public void setWrapperScriptName(String wrapperScriptName)
  {
    this.wrapperScriptName = wrapperScriptName;
  }

  public boolean isAdvancedStarter()
  {
    return advancedStarter.booleanValue();
  }

  public void setAdvancedStarter(boolean advancedStarter)
  {
    this.advancedStarter = Boolean.valueOf(advancedStarter);
  }

  public String getIzPackSrcDir()
  {
    return izPackSrcDir;
  }

  public void setIzPackSrcDir(String izPackSrcDir)
  {
    this.izPackSrcDir = izPackSrcDir;
  }

  public List getAuxFiles()
  {
    return auxFiles;
  }

  public void setAuxFiles(List auxFiles)
  {
    this.auxFiles = auxFiles;
  }

  public String getAuxFileSrcDir()
  {
    return auxFileSrcDir;
  }

  public void setAuxFileSrcDir(String auxFileSrcDir)
  {
    this.auxFileSrcDir = auxFileSrcDir;
  }

  public String getIzPackInstallerXml()
  {
    return izPackInstallerXml;
  }

  public void setIzPackInstallerXml(String izPackInstallerXml)
  {
    this.izPackInstallerXml = izPackInstallerXml;
  }

}
