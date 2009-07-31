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



package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.Collection;
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
   * Merges the <code>Collection</code>-based instances as follows:
   * <ul>
   * <li>if parent is non-null take parent else teh default collection</li>
   * <li>if child is non-null add all its contents</li>
   * </ul>
   * <p>That way you get either parent, default, parent plus child or default plus
   * child</p>
   */
  private static Collection merge(Collection child, Collection parent, Collection def)
  {
    Collection c = (parent != null ? parent : def);
    
    if (child != null)
      c.addAll(child);
    
    return c;
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
  
  Boolean createWindowsExecutable;
  
  Boolean createOSXApp;
  
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
   * Denotes a list of {@link AuxFile} instances specifying additional files
   * that need to be added to the package. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List auxFiles;

  /**
   * Denotes a path that is used for user-level executables (usually /usr/bin).
   * If <code>prefix</code> is used it is overriden by this value for binaries.
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * or the parent's value. In case the value is empty the distribution's
   * default bindir prepended by the prefix is used for executables!</p>
   */
  String bindir;

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
   * Denotes a set of dependencies (in Maven's artifact id naming) that should
   * be bundled with the application regardless of their existence in the target system's
   * native package management.
   * 
   * <p>Default is <code>null</code>, after merging it is an empty set
   * or the parent's value.</p>
   */
  Set bundleDependencies;

  /**
   * Denotes the directory in the target system where the bundled jar files are put.
   * 
   * <p>Default value is <code>null</code>, after merging it is the empty string (meaning
   * the default bundled jar dir is used) or the parent's value.
   */
  String bundledJarDir;

  /**
   * The distribution which is chosen to be built. This is not handled by
   * Maven2 but only by the Packaging class.
   */
  String chosenDistro;

  /**
   * Denotes the directory in the target system where application specific data files
   * are put.
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * or the parent's value. In case the value is empty the distribution's
   * default datadir prepended by the prefix is used.</p>
   */
  String datadir;

  /**
   * List of files which are installed into the application-specific data files
   * directory.
   */
  List dataFiles;

  /**
   * Denotes the root directory in the target system where application specific data
   * files are put. This is usually the directory one-level above the datadir.
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * or the parent's value. In case the value is empty the distribution's
   * default datarootdir prepended by the prefix is used.</p>
   */
  String datarootdir;

  /**
   * List of files which are installed into the root directory of application-specific
   * data files directory.
   * 
   * <p>By using this property one can install files into another application's datadir,
   * e.g. /usr/share/dbus-1
   */
  List datarootFiles;

  /**
   * Denotes the distributions this configuration is used for.
   */
  Set distros = new HashSet();

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
   * Denotes the name of the IzPack descriptor file.
   * 
   * <p>Default is <code>null</code>, after merging it is <code>installer.xml</code>
   * or the parent's value.</p>
   */
  String izPackInstallerXml;

  /**
   * Denotes a list of custom jar files. These are copied to their respective destination
   * suitable for the chosen target system. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List jarFiles;

  /**
   * Denotes a list of native libraries. These are copied to their respective destination
   * suitable for the chosen target system. 
   * 
   * <p>Default is <code>null</code>, after merging it is an empty list
   * or the parent's value.</p>
   */
  List jniFiles;

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
   * Denotes a list of strings which should be added to the "Recommends"-field of the package.
   * 
   * From the Debian Policy Manual (http://www.debian.org/doc/debian-policy/ch-relationships.html):
   * 
   * "This declares a strong, but not absolute, dependency. 
   * The Recommends field should list packages that would be found together with this one in all but unusual installations."
   * 
   * <p>Default is <code>null</code>
   */
  List recommends;
  
  /**
   * Denotes a list of strings which should be added to the "Suggests"-field of the package.
   * 
   * From the Debian Policy Manual (http://www.debian.org/doc/debian-policy/ch-relationships.html):
   * 
   * "This is used to declare that one package may be more useful with one or more others.
   * Using this field tells the packaging system and the user that the listed packages are related
   *  to this one and can perhaps enhance its usefulness, but that installing this one without them is perfectly reasonable."
   * 
   * <p>Default is <code>null</code>
   */
  List suggests;
  
  /**
   * Denotes a list of strings which should be added to the "Provides"-field of the package.
   * 
   * From the Debian Policy Manual (http://www.debian.org/doc/debian-policy/ch-relationships.html):
   * 
   * "A virtual package is one which appears in the Provides control file field of another package.
   * The effect is as if the package(s) which provide a particular virtual package name
   *  had been listed by name everywhere the virtual package name appears."
   * 
   * <p>Default is <code>null</code>
   */
  List provides;
  
  /**
   * Denotes a list of strings which should be added to the "Conflicts"-field of the package.
   * 
   * From the Debian Policy Manual (http://www.debian.org/doc/debian-policy/ch-relationships.html):
   * 
   * "When one binary package declares a conflict with another using a Conflicts field, dpkg will
   *  refuse to allow them to be installed on the system at the same time."
   * 
   * <p>Default is <code>null</code>
   */
  List conflicts;
  
  /**
   * Denotes a list of strings which should be added to the "Replaces"-field of the package.
   * 
   * From the Debian Policy Manual (http://www.debian.org/doc/debian-policy/ch-relationships.html):
   * 
   * "Packages can declare in their control file that they should overwrite files in certain other
   *  packages, or completely replace other packages"
   * 
   * <p>Default is <code>null</code>
   */
  List replaces;
  
  /**
   * Denotes the value of the "-Xmx" argument. 
   * 
   * <p>Default is <code>null</code>, after merging it is <code>null</code>
   * or the parent's value.</p>
   */
  String maxJavaMemory;
  
  /**
   * Specifies the distroconfiguration from which this one inherits all non-set values
   * or from which collections are merged.
   * 
   * <p>If unset it is <code>null</code> meaning the default distro configuration is the
   * sole parent.</p>
   */
  String parent;

  /**
   * Specifies the name of a file which is used as a post installation script.
   * 
   * <p>The base directory to look for the script is the aux files directory!</p>
   * 
   * <p>It is only valid for packaging system which support such scripts.</p>
   * 
   * <p>If unset it is <code>null</code> and no script is used.</p>
   */
  String postinstScript;
  
  /**
   * Specifies the name of a file which is used as a post removal script.
   * 
   * <p>The base directory to look for the script is the aux files directory!</p>
   * 
   * <p>It is only valid for packaging system which support such scripts.</p>
   * 
   * <p>If unset it is <code>null</code> and no script is used.</p>
   */
  String postrmScript;
  
  /**
   * Denotes a path that is prepended before all application paths.
   * 
   * <p>This allows installation to different directories as "/".</p>
   * 
   * <p>Default is <code>null</code>, after merging it is <code>/</code>
   * or the parent's value.</p>
   */
  String prefix;
  
  /**
   * Specifies the name of a file which is used as a pre-installlation script.
   * 
   * <p>The base directory to look for the script is the aux files directory!</p>
   * 
   * <p>It is only valid for packaging system which support such scripts.</p>
   * 
   * <p>If unset it is <code>null</code> and no script is used.</p>
   */
  String preinstScript;
 
  /**
   * Specifies the name of a file which is used as a pre-removal script.
   * 
   * <p>The base directory to look for the script is the aux files directory!</p>
   * 
   * <p>It is only valid for packaging system which support such scripts.</p>
   * 
   * <p>If unset it is <code>null</code> and no script is used.</p>
   */
  String prermScript;
  
  /**
   * Denotes the packages revision. This is a version number which appended after the real package
   * version and can be used to denote a change to the packaging (e.g. moved a file to the correct
   * location).
   * 
   * <p>It is possible to use all kinds of strings for that. The ordering rules of those is dependent
   * on the underlying packaging system. Try to use something sane like "r0", "r1" and so on.</p>
   * 
   * <p>If this value is not set or set to the empty string, no revision is appended.</p>
   *  
   * <p>Default is <code>null</code>, after merging it is the empty
   * or the parent's value.</p>
   */
  String revision;
  
  /**
   * Denotes the value of the section property supported by packaging systems.   
   * 
   * <p>Default is <code>null</code>, after merging it is "libs"
   * or the parent's value.</p>
   */
  String section;
  
  /**
   * Denotes the directory in which the packager looks for auxiliary files to
   * copy into the package.
   * 
   * <p>By default the aux files directory is meant to contain all the other
   * kinds of files like sysconf, dataroot and data files.</p> 
   * 
   * <p>By using this property one can define a common filename set which has
   * to be copied but works on different files since the <code>srcAuxFilesDir</code>
   * property can be changed on a per distribution basis.</p>
   * 
   * <p>Note: The path must be relative to the project's base dir.</p>
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * (meaning the default location (= <code<src/main/auxfiles</code>) is used
   * or the parent's value.</p>
   */
  String srcAuxFilesDir;
  
  /**
   * Denotes the source directory into which the packager looks for application
   * specific data files. 
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * (meaning the default location (= {@link #srcAuxFilesDir}) is used
   * or the parent's value.</p>
   */
  String srcDataFilesDir;
  
  /**
   * Denotes the source directory into which the packager looks for data files
   * which will be copied into the root directory of application specific data
   * files.  
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * (meaning the default location (= {@link #srcAuxFilesDir}) is used
   * or the parent's value.</p>
   */
  String srcDatarootFilesDir;

  /**
   * Denotes the source directory into which the packager looks for IzPack
   * specific datafiles.
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * (meaning the default location (= {@link #srcAuxFilesDir}) is used
   * or the parent's value.</p>
   */
  String srcIzPackFilesDir;

  /**
   * Denotes the directory in which the packager looks for Jar library files to
   * copy into the package.
   * 
   * <p>By using this property one can define a common filename set which has
   * to be copied but works on different files since the <code>srcJarFilesDir</code>
   * property can be changed on a per distribution basis.</p>
   * 
   * <p>Note: The path must be relative to the project's base dir.</p>
   * 
   * <p>Default is <code>null</code>, after merging it is an empty string
   * or the parent's value.</p>
   */
  String srcJarFilesDir;

  /**
   * Denotes the directory in which the packager looks for JNI library files to
   * copy into the package.
   * 
   * <p>By using this property one can define a common filename set which has
   * to be copied but works on different files since the <code>srcJNIFilesDir</code>
   * property can be changed on a per distribution basis.</p>
   * 
   * <p>Note: The path must be relative to the project's base dir.</p>
   * 
   * <p>Default is <code>null</code>, after merging it is an empty string
   * or the parent's value.</p>
   */
  String srcJNIFilesDir;
  
  String srcSysconfFilesDir;
  
  /**
   * Denotes a path that is used for user-level configuration data. If <code>prefix</code>
   * is used it is overriden by this value..
   * 
   * <p>Default is <code>null</code>, after merging it is the empty string
   * or the parent's value. In case the value is empty default sysconfdir (= /etc)
   * is prepended by the prefix!</p>
   */
  String sysconfdir;
  List sysconfFiles;
  
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

  public DistroConfiguration()
  {
    // For instantiation.
  }

  public String getArchitecture()
  {
    return architecture;
  }

  public List getAuxFiles()
  {
    return auxFiles;
  }

  public String getBindir()
  {
    return bindir;
  }

  public Set getBundleDependencies()
  {
    return bundleDependencies;
  }

  public String getDatadir()
  {
    return datadir;
  }

  public List getDataFiles()
  {
    return dataFiles;
  }

  public String getDatarootdir()
  {
    return datarootdir;
  }

  public List getDatarootFiles()
  {
    return datarootFiles;
  }

  public Set getDistros()
  {
    return distros;
  }

  public String getGcjDbToolExec()
  {
    return gcjDbToolExec;
  }

  public String getGcjExec()
  {
    return gcjExec;
  }

  public String getIzPackInstallerXml()
  {
    return izPackInstallerXml;
  }

  public List getJarFiles()
  {
    return jarFiles;
  }

  public List getJniFiles()
  {
    return jniFiles;
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
  
  public List getRecommends()
  {  
	return recommends;
  }
  
  public List getSuggests()
  {  
	return recommends;
  }
  
  public List getProvides()
  {  
	return provides;
  }
  
  public List getConflicts()
  {  
	return conflicts;
  }
  
  public List getReplaces()
  {  
	return replaces;
  }

  public String getMaxJavaMemory()
  {
    return maxJavaMemory;
  }

  public String getPostinstScript()
  {
    return postinstScript;
  }

  public String getPostrmScript()
  {
    return postrmScript;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public String getPreinstScript()
  {
    return preinstScript;
  }

  public String getPrermScript()
  {
    return prermScript;
  }
  
  public String getSection()
  {
    return section;
  }
  
  public String getRevision() {
	  return revision;
  }
  
  public String getChosenDistro() {
	  return chosenDistro;
  }

  public String getSrcAuxFilesDir()
  {
    return srcAuxFilesDir;
  }

  public String getSrcDataFilesDir()
  {
    return srcDataFilesDir;
  }

  public String getSrcDatarootFilesDir()
  {
    return srcDatarootFilesDir;
  }

  public String getSrcIzPackFilesDir()
  {
    return srcIzPackFilesDir;
  }

  public String getSrcJarFilesDir()
  {
    return srcJarFilesDir;
  }

  public String getSrcJNIFilesDir()
  {
    return srcJNIFilesDir;
  }

  public String getSrcSysconfFilesDir()
  {
    return srcSysconfFilesDir;
  }
  
  public String getSysconfdir()
  {
    return sysconfdir;
  }

  public List getSysconfFiles()
  {
    return sysconfFiles;
  }

  public Properties getSystemProperties()
  {
    return systemProperties;
  }

  public String getWrapperScriptName()
  {
    return wrapperScriptName;
  }

  public boolean isAdvancedStarter()
  {
    return advancedStarter.booleanValue();
  }

  public boolean isAotCompile()
  {
    return aotCompile.booleanValue();
  }

  public boolean isBundleAll()
  {
    return bundleAll.booleanValue();
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
    /* Note: The fields chosenDistro, distros and parent are not merged
     * because they are the header or descriptor of the configuration not
     * its data.
     */
    
    aotCompile = (Boolean) merge(aotCompile, parent.aotCompile, Boolean.FALSE);
    bundleAll = (Boolean) merge(bundleAll, parent.bundleAll, Boolean.FALSE);
    advancedStarter = (Boolean) merge(advancedStarter, parent.advancedStarter, Boolean.FALSE);

    createOSXApp = (Boolean) merge(createOSXApp, parent.createOSXApp, Boolean.TRUE);
    createWindowsExecutable = (Boolean) merge(createWindowsExecutable, parent.createWindowsExecutable, Boolean.TRUE);

    prefix = (String) merge(prefix, parent.prefix, "/");
    bindir = (String) merge(bindir, parent.bindir, "");
    sysconfdir = (String) merge(sysconfdir, parent.sysconfdir, "");
    datarootdir = (String) merge(datarootdir, parent.datarootdir, "");
    datadir = (String) merge(datadir, parent.datadir, "");

    bundledJarDir = (String) merge(bundledJarDir, parent.bundledJarDir, "");

    architecture = (String) merge(architecture, parent.architecture, "all");
    gcjDbToolExec = (String) merge(gcjDbToolExec, parent.gcjDbToolExec,
                                      "gcj");
    gcjExec = (String) merge(gcjExec, parent.gcjExec, "gcj-dbtool");

    jniLibraryPath = (String) merge(jniLibraryPath, parent.jniLibraryPath,
                                    "/usr/lib/jni");
    
    mainClass = (String) merge(mainClass, parent.mainClass, null);
    revision = (String) merge(revision, parent.revision, "");
    wrapperScriptName = (String) merge(wrapperScriptName, parent.wrapperScriptName, null);
    maintainer = (String) merge(maintainer, parent.maintainer, null);
    maxJavaMemory = (String) merge(maxJavaMemory, parent.maxJavaMemory, null);
    section = (String) merge(section, parent.section, "libs");
    izPackInstallerXml = (String) merge(izPackInstallerXml, parent.izPackInstallerXml, "installer.xml");
    
    preinstScript = (String) merge(preinstScript, parent.preinstScript, null);
    prermScript = (String) merge(prermScript, parent.prermScript, null);
    postinstScript = (String) merge(postinstScript, parent.postinstScript, null);
    postrmScript = (String) merge(postrmScript, parent.postrmScript, null);
    
    srcAuxFilesDir = (String) merge(srcAuxFilesDir, parent.srcAuxFilesDir, "");
    srcSysconfFilesDir = (String) merge(srcSysconfFilesDir, parent.srcSysconfFilesDir, "");
    srcJarFilesDir = (String) merge(srcJarFilesDir, parent.srcJarFilesDir, "");
    srcJNIFilesDir = (String) merge(srcJNIFilesDir, parent.srcJNIFilesDir, "");
    srcDatarootFilesDir = (String) merge(srcDatarootFilesDir, parent.srcDatarootFilesDir, "");
    srcDataFilesDir = (String) merge(srcDataFilesDir, parent.srcDataFilesDir, "");

    srcIzPackFilesDir = (String) merge(srcIzPackFilesDir, parent.srcIzPackFilesDir, "");
    
    auxFiles = (List) merge(auxFiles, parent.auxFiles,
                                new ArrayList());

    sysconfFiles = (List) merge(sysconfFiles, parent.sysconfFiles,
                            new ArrayList());

    jarFiles = (List) merge(jarFiles, parent.jarFiles,
            new ArrayList());

    jniFiles = (List) merge(jniFiles, parent.jniFiles,
                                   new ArrayList());

    datarootFiles = (List) merge(datarootFiles, parent.datarootFiles,
                            new ArrayList());

    dataFiles = (List) merge(dataFiles, parent.dataFiles,
                                 new ArrayList());
    
    bundleDependencies = (Set) merge(bundleDependencies,
                                     parent.bundleDependencies,
                                     new HashSet());

    manualDependencies = (List) merge(manualDependencies, parent.manualDependencies,
                                         new ArrayList());
    
    recommends = (List) merge(recommends, parent.recommends,
            new ArrayList());
    
    suggests = (List) merge(suggests, parent.suggests,
            new ArrayList());
    
    provides = (List) merge(provides, parent.provides,
            new ArrayList());
    
    conflicts = (List) merge(conflicts, parent.conflicts,
            new ArrayList());
    
    replaces = (List) merge(replaces, parent.replaces,
            new ArrayList());

    systemProperties = (Properties) merge(systemProperties,
                                             parent.systemProperties,
                                             new Properties());

    return this;
  }

  public void setAdvancedStarter(boolean advancedStarter)
  {
    this.advancedStarter = Boolean.valueOf(advancedStarter);
  }

  public void setAotCompile(boolean aotCompile)
  {
    this.aotCompile = Boolean.valueOf(aotCompile);
  }

  public void setArchitecture(String architecture)
  {
    this.architecture = architecture;
  }

  public void setAuxFiles(List auxFiles)
  {
    this.auxFiles = auxFiles;
  }

  public void setBindir(String bindir)
  {
    this.bindir = bindir;
  }

  public void setBundleAll(boolean bundleAll)
  {
    this.bundleAll = Boolean.valueOf(bundleAll);
  }

  public void setBundleDependencies(Set bundleDependencies)
  {
    this.bundleDependencies = bundleDependencies;
  }

  public void setDatadir(String datadir)
  {
    this.datadir = datadir;
  }

  public void setDataFiles(List dataFiles)
  {
    this.dataFiles = dataFiles;
  }

  public void setDatarootdir(String datarootdir)
  {
    this.datarootdir = datarootdir;
  }

  public void setDatarootFiles(List datarootFiles)
  {
    this.datarootFiles = datarootFiles;
  }

  public void setDistro(String distro)
  {
    distros.add(distro);
  }

  public void setDistros(Set distros)
  {
    this.distros = distros;
  }

  public void setGcjDbToolExec(String gcjDbToolExec)
  {
    this.gcjDbToolExec = gcjDbToolExec;
  }

  public void setGcjExec(String gcjExec)
  {
    this.gcjExec = gcjExec;
  }

  public void setIzPackInstallerXml(String izPackInstallerXml)
  {
    this.izPackInstallerXml = izPackInstallerXml;
  }

  public void setJarFiles(List jarLibraries)
  {
    this.jarFiles = jarLibraries;
  }

  public void setJniFiles(List jniLibraries)
  {
    this.jniFiles = jniLibraries;
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
  
  public void setRecommends(List recommends)
  {
	this.recommends = recommends;
  }
  
  public void setSuggests(List suggests)
  {
    this.suggests = suggests;
  }
  
  public void setProvides(List provides)
  {
    this.provides = provides;
  }
  
  public void setConflicts(List conflicts)
  {
    this.conflicts = conflicts;
  }
  
  public void setReplaces(List replaces)
  {
    this.replaces = replaces;
  }

  public void setMaxJavaMemory(String maxJavaMemory)
  {
    this.maxJavaMemory = maxJavaMemory;
  }

  public void setPostinstScript(String postinstScript)
  {
    this.postinstScript = postinstScript;
  }

  public void setPostrmScript(String postrmScript)
  {
    this.postrmScript = postrmScript;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  public void setPreinstScript(String preinstScript)
  {
    this.preinstScript = preinstScript;
  }

  public void setPrermScript(String prermScript)
  {
    this.prermScript = prermScript;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public void setSrcAuxFilesDir(String auxFileSrcDir)
  {
    this.srcAuxFilesDir = auxFileSrcDir;
  }

  public void setSrcDataFilesDir(String srcDataFilesDir)
  {
    this.srcDataFilesDir = srcDataFilesDir;
  }

  public void setSrcDatarootFilesDir(String srcDatarootFilesDir)
  {
    this.srcDatarootFilesDir = srcDatarootFilesDir;
  }

  public void setSrcIzPackFilesDir(String srcIzPackFilesDir)
  {
    this.srcIzPackFilesDir = srcIzPackFilesDir;
  }

  public void setSrcJarFilesDir(String srcJarFilesDir)
  {
    this.srcJarFilesDir = srcJarFilesDir;
  }

  public void setSrcJNIFilesDir(String srcJNIFilesDir)
  {
    this.srcJNIFilesDir = srcJNIFilesDir;
  }

  public void setSrcSysconfFilesDir(String srcSysconfFilesDir)
  {
    this.srcSysconfFilesDir = srcSysconfFilesDir;
  }

  public void setSysconfdir(String sysconfdir)
  {
    this.sysconfdir = sysconfdir;
  }

  public void setSysconfFiles(List sysconfFiles)
  {
    this.sysconfFiles = sysconfFiles;
  }

  public void setSystemProperties(Properties systemProperties)
  {
    this.systemProperties = systemProperties;
  }

  public void setWrapperScriptName(String wrapperScriptName)
  {
    this.wrapperScriptName = wrapperScriptName;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("distros:");
    if (!distros.isEmpty())
      {
        Iterator ite = distros.iterator();
        while (ite.hasNext())
          sb.append(ite.next());
        sb.append("\n");
      }

    sb.append("chosenDistro: " + chosenDistro + "\n");
    
    sb.append("architecture: " + architecture + "\n");
    sb.append("prefix: " + prefix + "\n");
    sb.append("bindir: " + bindir + "\n");
    sb.append("sysconfdir: " + sysconfdir + "\n");
    sb.append("datarootdir: " + datarootdir + "\n");
    sb.append("datadir: " + datarootdir + "\n");
    sb.append("bundledJarDir: " + bundledJarDir + "\n");
    sb.append("aotCompile: " + aotCompile + "\n");
    sb.append("bundleAll: " + bundleAll + "\n");
    sb.append("advancedStarter: " + advancedStarter + "\n");

    sb.append("gcjDbToolExec: " + gcjDbToolExec + "\n");
    sb.append("gcjExec: " + gcjExec + "\n");
    sb.append("jarFiles: " + jarFiles + "\n");
    sb.append("jniFiles: " + jniFiles + "\n");
    sb.append("jniLibraryPath: " + jniLibraryPath + "\n");
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
    
    sb.append("datarootFiles:");
    if (datarootFiles != null)
      {
        Iterator ite = datarootFiles.iterator();
        while (ite.hasNext())
          {
            AuxFile af = (AuxFile) ite.next();
            sb.append(af.from + " -> " + af.to);
            sb.append("\n");
          }
      }
    
    sb.append("dataFiles:");
    if (dataFiles != null)
      {
        Iterator ite = dataFiles.iterator();
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

  public boolean isCreateOSXApp()
  {
    return createOSXApp.booleanValue();
  }

  public void setCreateOSXApp(boolean createOSXApp)
  {
    this.createOSXApp = Boolean.valueOf(createOSXApp);
  }

  public boolean isCreateWindowsExecutable()
  {
    return createWindowsExecutable.booleanValue();
  }

  public void setCreateWindowsExecutable(boolean createWindowsExecutable)
  {
    this.createWindowsExecutable = Boolean.valueOf(createWindowsExecutable);
  }

}
