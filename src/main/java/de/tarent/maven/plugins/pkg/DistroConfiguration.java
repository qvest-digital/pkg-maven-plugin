

package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class DistroConfiguration
{
  String distro;

  Boolean aotCompile;

  String architecture;

  Boolean advancedStarter;

  Boolean bundleAll;

  String auxFileSrcDir;

  String gcjDbToolExec;

  String gcjExec;

  Set bundleDependencies;

  List jniLibraries;

  String jniLibraryPath;

  String mainClass;

  String maintainer;

  List manualDependencies;
  
  List auxFiles;

  String maxJavaMemory;

  Properties resources;

  String section;

  Properties systemProperties;
  
  String wrapperScriptName;
  
  String izPackSrcDir;
  
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

  public Properties getResources()
  {
    return resources;
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

  public void setResources(Properties resources)
  {
    this.resources = resources;
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

    sb.append("resources:\n");
    sb.append(resources);
    sb.append("\n");

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

    resources = (Properties) merge(resources, parent.resources,
                                      new Properties());
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
