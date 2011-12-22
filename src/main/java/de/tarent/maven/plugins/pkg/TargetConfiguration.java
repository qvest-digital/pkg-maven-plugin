/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb, ipk, izpack)
 * Copyright (C) 2000-2009 tarent GmbH
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;

import de.tarent.maven.plugins.pkg.annotations.MergeMe;


/**
 * A <code>TargetConfiguration</code> provides the properties to configure the
 * packaging for a particular target.
 * 
 * <p>A target is a much more fine granular entity than a distribution. E.g. it may
 * denote a certain piece of hardware.<p> 
 * 
 * <p>
 * Except for the boolean properties every field can be accessed directly. The
 * boolean properties are using <code>Boolean</code> to allow them to be
 * <code>null</code> which means 'not set' and is an important state for the
 * merging of two <code>DistroConfiguration</code> instances.</p>
 * 
 * <p>
 * A TargetConfiguration may only be used once it has been merged or fixated. 
 * Otherwise unwanted behaviour may occur.</p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 * 
 */
public class TargetConfiguration {
	
	public TargetConfiguration(String target) {
		this.target = target;
	}	
	/**
	 * Denotes the target this configuration is for.
	 */
	@MergeMe
	private String target;

	@MergeMe(defaultBoolean=true)
	private Boolean createWindowsExecutable;

	@MergeMe(defaultBoolean=true)
	private Boolean createOSXApp;

	/**
	 * Denotes whether the packager should use a special starter class to run
	 * the application which allows working around platform limitations as fixed
	 * command-line length.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>false</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultBoolean=false)
	private Boolean advancedStarter;

	/**
	 * Denotes wether the packager should invoke ahead of time compilation (if
	 * it supports this).
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>false</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultBoolean=false)
	private Boolean aotCompile;

	/**
	 * Denotes the architecure string to be used. This is only effective for
	 * packagers supporting this feature (= ipk, deb).
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>all</code> or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="all")
	private String architecture;

	/**
	 * Denotes a list of {@link AuxFile} instances specifying additional files
	 * that need to be added to the package.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty list or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private List<AuxFile> auxFiles;

	/**
	 * Denotes a path that is used for user-level executables (usually
	 * /usr/bin). If <code>prefix</code> is used it is overriden by this value
	 * for binaries.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string or the
	 * parent's value. In case the value is empty the distribution's default
	 * bindir prepended by the prefix is used for executables!
	 * </p>
	 */
	@MergeMe(defaultString="")
	private String bindir;
	
	/**
	 * List of files which are installed into the directory for executable.
	 */
	@MergeMe
	private List<BinFile> binFiles;

	/**
	 * Denotes whether the packager should bundle every dependency regardless of
	 * whether a particular item is available from the system's native package
	 * management or not. This can be used to work around problems with those
	 * packages.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>false</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultBoolean=false)
	private Boolean bundleAll;

	/**
	 * Denotes a set of dependencies (in Maven's artifact id naming) that should
	 * be bundled with the application regardless of their existence in the
	 * target system's native package management.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty set or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private Set<String> bundleDependencies;

	/**
	 * Denotes the directory in the target system where the bundled jar files
	 * are put.
	 * 
	 * <p>
	 * Default value is <code>null</code>, after merging it is the empty string
	 * (meaning the default bundled jar dir is used) or the parent's value.
	 */
	@MergeMe(defaultString="")
	private String bundledJarDir;

	/**
	 * The distribution which is chosen to be built. This is not handled by
	 * Maven2 but only by the Packaging class.
	 */
	private String chosenDistro;

	/**
	 * Denotes the directory in the target system where application specific
	 * data files are put.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string or the
	 * parent's value. In case the value is empty the distribution's default
	 * datadir prepended by the prefix is used.
	 * </p>
	 */
	@MergeMe(defaultString="")
	private String datadir;

	/**
	 * List of files which are installed into the application-specific data
	 * files directory.
	 */
	@MergeMe
	private List<DataFile> dataFiles;

	/**
	 * Denotes the root directory in the target system where application
	 * specific data files are put. This is usually the directory one-level
	 * above the datadir.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string or the
	 * parent's value. In case the value is empty the distribution's default
	 * datarootdir prepended by the prefix is used.
	 * </p>
	 */
	@MergeMe(defaultString="")
	private String datarootdir;

	/**
	 * List of files which are installed into the root directory of
	 * application-specific data files directory.
	 * 
	 * <p>
	 * By using this property one can install files into another application's
	 * datadir, e.g. /usr/share/dbus-1
	 */
	@MergeMe
	private List<DatarootFile> datarootFiles;

	/**
	 * Denotes the distributions this configuration is used for.
	 */
	@MergeMe
	private Set<String> distros = new HashSet<String>();


	/**
	 * Set default distribution to package for.
	 * 
	 */
	@MergeMe(defaultValueIsNull=true)
	private String defaultDistro;
	
	/**
	 * Denotes the name of the gcj-dbtool executable. This allows the use of
	 * e.g. "gcj-dbtool-4.2" or "gcj-dbtool-4.3" depending on the targeted
	 * distribution.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>gcj-dbtool</code>
	 * or the parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="gcj")
	private String gcjDbToolExec;

	/**
	 * Denotes the name of the gcj executable. This allows the use of e.g.
	 * "gcj-4.2" or "gcj-4.3" depending on the targeted distribution.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>gcj</code> or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="gcj-dbtool")
	private String gcjExec;

	/**
	 * Denotes the name of the IzPack descriptor file.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is
	 * <code>installer.xml</code> or the parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="installer.xml")
	public String izPackInstallerXml;

	/**
	 * Denotes a list of custom jar files. These are copied to their respective
	 * destination suitable for the chosen target system.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty list or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private List<JarFile> jarFiles;

	/**
	 * Denotes a list of native libraries. These are copied to their respective
	 * destination suitable for the chosen target system.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty list or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private List<JniFile> jniFiles;

	/**
	 * Denotes the <code>java.library.path</code> value of the application. In
	 * case of IzPack packaging do not forget to use the "$install_path"
	 * variable.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty string or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="/usr/lib/jni")
	private String jniLibraryPath;

	/**
	 * Denotes the applications' main class. It can be different per
	 * distribution, which might be handy for different start screens or
	 * workarounds.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>null</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String mainClass;

	/**
	 * Denotes the value of the maintainer field in common packaging systems. It
	 * is basically an email address.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>null</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String maintainer;

	/**
	 * Denotes a list of dependency strings which should be added to the
	 * automatically generated ones. This allows to specify dependencies which
	 * Maven does not know about.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty list or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private List<String> manualDependencies;

	/**
	 * Denotes a list of strings which should be added to the "Recommends"-field
	 * of the package.
	 * 
	 * From the Debian Policy Manual
	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
	 * 
	 * "This declares a strong, but not absolute, dependency. The Recommends
	 * field should list packages that would be found together with this one in
	 * all but unusual installations."
	 * 
	 * <p>
	 * Default is <code>null</code>
	 */
	@MergeMe
	private List<String> recommends;

	/**
	 * Denotes a list of strings which should be added to the "Suggests"-field
	 * of the package.
	 * 
	 * From the Debian Policy Manual
	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
	 * 
	 * "This is used to declare that one package may be more useful with one or
	 * more others. Using this field tells the packaging system and the user
	 * that the listed packages are related to this one and can perhaps enhance
	 * its usefulness, but that installing this one without them is perfectly
	 * reasonable."
	 * 
	 * <p>
	 * Default is <code>null</code>
	 */
	@MergeMe
	private List<String> suggests;

	/**
	 * Denotes a list of strings which should be added to the "Provides"-field
	 * of the package.
	 * 
	 * From the Debian Policy Manual
	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
	 * 
	 * "A virtual package is one which appears in the Provides control file
	 * field of another package. The effect is as if the package(s) which
	 * provide a particular virtual package name had been listed by name
	 * everywhere the virtual package name appears."
	 * 
	 * <p>
	 * Default is <code>null</code>
	 */
	@MergeMe
	private List<String> provides;

	/**
	 * Denotes a list of strings which should be added to the "Conflicts"-field
	 * of the package.
	 * 
	 * From the Debian Policy Manual
	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
	 * 
	 * "When one binary package declares a conflict with another using a
	 * Conflicts field, dpkg will refuse to allow them to be installed on the
	 * system at the same time."
	 * 
	 * <p>
	 * Default is <code>null</code>
	 */
	@MergeMe
	private List<String> conflicts;

	/**
	 * Denotes a list of strings which should be added to the "Replaces"-field
	 * of the package.
	 * 
	 * From the Debian Policy Manual
	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
	 * 
	 * "Packages can declare in their control file that they should overwrite
	 * files in certain other packages, or completely replace other packages"
	 * 
	 * <p>
	 * Default is <code>null</code>
	 */
	@MergeMe
	private List<String> replaces;

	/**
	 * Denotes the value of the "-Xmx" argument.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>null</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe
	private String maxJavaMemory;

	/**
	 * Specifies the {@link TargetConfiguration} from which this one inherits all
	 * non-set values or from which collections are merged.
	 * 
	 */
	String parent;

	/**
	 * Specifies the name of a file which is used as a post installation script.
	 * 
	 * <p>
	 * The base directory to look for the script is the aux files directory!
	 * </p>
	 * 
	 * <p>
	 * It is only valid for packaging system which support such scripts.
	 * </p>
	 * 
	 * <p>
	 * If unset it is <code>null</code> and no script is used.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String postinstScript;

	/**
	 * Specifies the name of a file which is used as a post removal script.
	 * 
	 * <p>
	 * The base directory to look for the script is the aux files directory!
	 * </p>
	 * 
	 * <p>
	 * It is only valid for packaging system which support such scripts.
	 * </p>
	 * 
	 * <p>
	 * If unset it is <code>null</code> and no script is used.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String postrmScript;

	/**
	 * Denotes a path that is prepended before all application paths.
	 * 
	 * <p>
	 * This allows installation to different directories as "/".
	 * </p>
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>/</code> or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="/")
	private String prefix;

	/**
	 * Specifies the name of a file which is used as a pre-installlation script.
	 * 
	 * <p>
	 * The base directory to look for the script is the aux files directory!
	 * </p>
	 * 
	 * <p>
	 * It is only valid for packaging system which support such scripts.
	 * </p>
	 * 
	 * <p>
	 * If unset it is <code>null</code> and no script is used.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String preinstScript;

	/**
	 * Specifies the name of a file which is used as a pre-removal script.
	 * 
	 * <p>
	 * The base directory to look for the script is the aux files directory!
	 * </p>
	 * 
	 * <p>
	 * It is only valid for packaging system which support such scripts.
	 * </p>
	 * 
	 * <p>
	 * If unset it is <code>null</code> and no script is used.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String prermScript;

	/**
	 * Denotes the packages revision. This is a version number which appended
	 * after the real package version and can be used to denote a change to the
	 * packaging (e.g. moved a file to the correct location).
	 * 
	 * <p>
	 * It is possible to use all kinds of strings for that. The ordering rules
	 * of those is dependent on the underlying packaging system. Try to use
	 * something sane like "r0", "r1" and so on.
	 * </p>
	 * 
	 * <p>
	 * If this value is not set or set to the empty string, no revision is
	 * appended.
	 * </p>
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe(defaultString="r0")
	private String revision;
	
	/**
	 * Denotes a suffix which is added to the package name.
	 * 
	 */
	@MergeMe(defaultValueIsNull=true)
	private String packageNameSuffix;

	/**
	 * Denotes the value of the section property supported by packaging systems.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is "libs" or the parent's
	 * value.
	 * </p>
	 */
	@MergeMe(defaultString="libs")
	private String section;

	/**
	 * Denotes the directory in which the packager looks for auxiliary files to
	 * copy into the package.
	 * 
	 * <p>
	 * By default the aux files directory is meant to contain all the other
	 * kinds of files like sysconf, dataroot and data files.
	 * </p>
	 * 
	 * <p>
	 * By using this property one can define a common filename set which has to
	 * be copied but works on different files since the
	 * <code>srcAuxFilesDir</code> property can be changed on a per distribution
	 * basis.
	 * </p>
	 * 
	 * <p>
	 * Note: The path must be relative to the project's base dir.
	 * </p>
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string
	 * (meaning the default location (= <code<src/main/auxfiles</code>) is used
	 * or the parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcAuxFilesDir;

	/**
	 * Denotes the source directory into which the packager looks for
	 * executable files.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string
	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcBinFilesDir;

	/**
	 * Denotes the source directory into which the packager looks for
	 * application specific data files.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string
	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcDataFilesDir;

	/**
	 * Denotes the source directory into which the packager looks for data files
	 * which will be copied into the root directory of application specific data
	 * files.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string
	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcDatarootFilesDir;

	/**
	 * Denotes the source directory into which the packager looks for IzPack
	 * specific datafiles.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string
	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcIzPackFilesDir;

	/**
	 * Denotes the directory in which the packager looks for Jar library files
	 * to copy into the package.
	 * 
	 * <p>
	 * By using this property one can define a common filename set which has to
	 * be copied but works on different files since the
	 * <code>srcJarFilesDir</code> property can be changed on a per distribution
	 * basis.
	 * </p>
	 * 
	 * <p>
	 * Note: The path must be relative to the project's base dir.
	 * </p>
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty string or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcJarFilesDir;

	/**
	 * Denotes the directory in which the packager looks for JNI library files
	 * to copy into the package.
	 * 
	 * <p>
	 * By using this property one can define a common filename set which has to
	 * be copied but works on different files since the
	 * <code>srcJNIFilesDir</code> property can be changed on a per distribution
	 * basis.
	 * </p>
	 * 
	 * <p>
	 * Note: The path must be relative to the project's base dir.
	 * </p>
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty string or the
	 * parent's value.
	 * </p>
	 */
	@MergeMe
	private String srcJNIFilesDir;

	@MergeMe
	private String srcSysconfFilesDir;

	/**
	 * Denotes a path that is used for user-level configuration data. If
	 * <code>prefix</code> is used it is overriden by this value..
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is the empty string or the
	 * parent's value. In case the value is empty default sysconfdir (= /etc) is
	 * prepended by the prefix!
	 * </p>
	 */
	@MergeMe
	private String sysconfdir;

	@MergeMe
	private List<SysconfFile> sysconfFiles;

	/**
	 * Denotes a bunch of system properties keys and their values which are
	 * added to the starter script and thus provided to the application.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is an empty
	 * <code>Properties</code> instance or the parent's value.
	 * </p>
	 */
	@MergeMe
	private Properties systemProperties;

	/**
	 * Denotes the name of the wrapper script that is used to run the
	 * application. This property is optional and will default to the
	 * <code>artifactId</code> is the Maven project. For Windows targets ".bat"
	 * is appended to this name.
	 * 
	 * <p>
	 * Default is <code>null</code>, after merging it is <code>null</code> or
	 * the parent's value.
	 * </p>
	 */
	@MergeMe(defaultValueIsNull=true)
	private String wrapperScriptName;

	/**
	 * Denotes a piece of shell script code which is added to the (Unix) start
	 * script.
	 * 
	 * <p>
	 * The code is executed <em>after</em> the variables have been set
	 * (classpath, bootclasspath, system properties, ...) and right
	 * <em>before</em> the VM is started.
	 * </p>
	 */
	@MergeMe
	private String customCodeUnix;

	/**
	 * Denotes a piece of batch file code which is added to the (Windows) start
	 * script.
	 * 
	 * <p>
	 * The code is executed <em>after</em> the variables have been set
	 * (classpath, bootclasspath, system properties, ...) and right
	 * <em>before</em> the VM is started.
	 * </p>
	 */
	@MergeMe
	private String customCodeWindows;
	
	/**
	 * Denothes wether the package should be signed or not. As of now, this value is
	 * only taken in consideration when building RPM packages.
	 * 
	 * <p>Default value is <code>false</code>.</p>
	 * 
	 */
	@MergeMe(defaultBoolean=false)
	private Boolean sign;
	
	/**
	 * Denotes the release of the package to build. As of now, this value is
	 * only taken in consideration when building RPM packages.
	 * 
	 * <p>Default value is <code>unknown</code>.</p>
	 * 
	 */
	@MergeMe(defaultString="unknown")
	private String release;
	
	/**
	 * Denothes the source of the package to build. As of now, this value is
	 * only taken in consideration when building RPM packages.
	 * 
	 * <p>Default value is <code>unknown</code>.</p>
	 * 
	 */
	@MergeMe
	private String source;
	
	private UploadParameters uploadParameters;
	
	/**
	 * Denotes dependencies to other target configurations.
	 */
	@MergeMe
	private List<String> relations;
	
	/**
	 *  Denotes of this configuration is ready to be used. This flag is only set
	 *  if this configuration has been merged at least once or "fixated". Otherwise
	 *  some members needed for the configuration to be used may not have been 
	 *  initialized properly.
	 */
	private boolean ready;
	
	public TargetConfiguration() {
		// Intentionally empty.
	}

	public String getArchitecture() {
		checkIfReady();
		return architecture;
	}

	public List<? extends AuxFile> getAuxFiles() {
		checkIfReady();
		return auxFiles;
	}

	public String getBindir() {
		checkIfReady();
		return bindir;
	}

	public Set<String> getBundleDependencies() {
		checkIfReady();
		return bundleDependencies;
	}

	public String getDatadir() {
		checkIfReady();
		return datadir;
	}

	public List<? extends DataFile> getDataFiles() {
		checkIfReady();
		return dataFiles;
	}
	
	public List<? extends BinFile> getBinFiles() {
		checkIfReady();
		return binFiles;
	}

	public String getDatarootdir() {
		checkIfReady();
		return datarootdir;
	}

	public List<? extends DatarootFile> getDatarootFiles() {
		checkIfReady();
		return datarootFiles;
	}

	public Set<String> getDistros() {
		// No readyness check as the Utils#getMergedConfiguration() method
		// depends on this field being available.
		return distros;
	}

	public String getGcjDbToolExec() {
		checkIfReady();
		return gcjDbToolExec;
	}

	public String getGcjExec() {
		checkIfReady();
		return gcjExec;
	}

	public String getIzPackInstallerXml() {
		checkIfReady();
		return izPackInstallerXml;
	}

	public List<JarFile> getJarFiles() {
		checkIfReady();
		return jarFiles;
	}

	public List<? extends AuxFile> getJniFiles() {
		checkIfReady();
		return jniFiles;
	}

	public String getJniLibraryPath() {
		checkIfReady();
		return jniLibraryPath;
	}

	public String getMainClass() {
		checkIfReady();
		return mainClass;
	}

	public String getMaintainer() {
		checkIfReady();
		return maintainer;
	}

	public List<String> getManualDependencies() {
		checkIfReady();
		return manualDependencies;
	}

	public List<String> getRecommends() {
		checkIfReady();
		return recommends;
	}

	public List<String> getSuggests() {
		checkIfReady();
		return recommends;
	}

	public List<String> getProvides() {
		checkIfReady();
		return provides;
	}

	public List<String> getConflicts() {
		checkIfReady();
		return conflicts;
	}

	public List<String> getReplaces() {
		checkIfReady();
		return replaces;
	}

	public String getMaxJavaMemory() {
		checkIfReady();
		return maxJavaMemory;
	}

	public String getPostinstScript() {
		checkIfReady();
		return postinstScript;
	}

	public String getPostrmScript() {
		checkIfReady();
		return postrmScript;
	}

	public String getPrefix() {
		checkIfReady();
		return prefix;
	}

	public String getPreinstScript() {
		checkIfReady();
		return preinstScript;
	}

	public String getPrermScript() {
		checkIfReady();
		return prermScript;
	}

	public String getSection() {
		checkIfReady();
		return section;
	}

	public String getRevision() {
		checkIfReady();
		if(revision == null){
			return "";
		}else{
			return revision;
		}
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getChosenDistro() {
		checkIfReady();
		return chosenDistro;
	}

	public String getSrcAuxFilesDir() {
		checkIfReady();
		return srcAuxFilesDir;
	}

	public String getSrcBinFilesDir() {
		checkIfReady();
		return srcBinFilesDir;
	}

	public String getSrcDataFilesDir() {
		checkIfReady();
		return srcDataFilesDir;
	}

	public String getSrcDatarootFilesDir() {
		checkIfReady();
		return srcDatarootFilesDir;
	}

	public String getSrcIzPackFilesDir() {
		checkIfReady();
		return srcIzPackFilesDir;
	}

	public String getSrcJarFilesDir() {
		checkIfReady();
		return srcJarFilesDir;
	}

	public String getSrcJNIFilesDir() {
		checkIfReady();
		return srcJNIFilesDir;
	}

	public String getSrcSysconfFilesDir() {
		checkIfReady();
		return srcSysconfFilesDir;
	}

	public String getSysconfdir() {
		checkIfReady();
		return sysconfdir;
	}

	public List<SysconfFile> getSysconfFiles() {
		checkIfReady();
		return sysconfFiles;
	}

	public Properties getSystemProperties() {
		checkIfReady();
		return systemProperties;
	}

	public String getWrapperScriptName() {
		checkIfReady();
		return wrapperScriptName;
	}

	public boolean isAdvancedStarter() {
		checkIfReady();
		return advancedStarter.booleanValue();
	}

	public boolean isAotCompile() {
		checkIfReady();
		return aotCompile.booleanValue();
	}

	public boolean isBundleAll() {
		checkIfReady();
		return bundleAll.booleanValue();
	}

	public void setAdvancedStarter(boolean advancedStarter) {
		this.advancedStarter = Boolean.valueOf(advancedStarter);
	}

	public void setAotCompile(boolean aotCompile) {
		this.aotCompile = Boolean.valueOf(aotCompile);
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public void setAuxFiles(List<AuxFile> auxFiles) {
		this.auxFiles = auxFiles;
	}

	public void setBindir(String bindir) {
		this.bindir = bindir;
	}

	public void setBundleAll(boolean bundleAll) {
		this.bundleAll = Boolean.valueOf(bundleAll);
	}

	public void setBundleDependencies(Set<String> bundleDependencies) {
		this.bundleDependencies = bundleDependencies;
	}

	public void setDatadir(String datadir) {
		this.datadir = datadir;
	}

	public void setDataFiles(List<DataFile> dataFiles) {
		this.dataFiles = dataFiles;
	}
	
	public void setBinFiles(List<BinFile> binFiles) {
		this.binFiles = binFiles;
	}

	public void setDatarootdir(String datarootdir) {
		this.datarootdir = datarootdir;
	}

	public void setDatarootFiles(List<DatarootFile> datarootFiles) {
		this.datarootFiles = datarootFiles;
	}

	public void setDistro(String distro) {
		distros.add(distro);
	}

	public void setDistros(Set<String> distros) {
		this.distros = distros;
	}

	public void setGcjDbToolExec(String gcjDbToolExec) {
		this.gcjDbToolExec = gcjDbToolExec;
	}

	public void setGcjExec(String gcjExec) {
		this.gcjExec = gcjExec;
	}

	public void setIzPackInstallerXml(String izPackInstallerXml) {
		this.izPackInstallerXml = izPackInstallerXml;
	}

	public void setJarFiles(List<JarFile> jarLibraries) {
		this.jarFiles = jarLibraries;
	}

	public void setJniFiles(List<JniFile> jniLibraries) {
		this.jniFiles = jniLibraries;
	}

	public void setJniLibraryPath(String jniLibraryPath) {
		this.jniLibraryPath = jniLibraryPath;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public void setManualDependencies(List<String> manualDependencies) {
		this.manualDependencies = manualDependencies;
	}

	public void setRecommends(List<String> recommends) {
		this.recommends = recommends;
	}

	public void setSuggests(List<String> suggests) {
		this.suggests = suggests;
	}

	public void setProvides(List<String> provides) {
		this.provides = provides;
	}

	public void setConflicts(List<String> conflicts) {
		this.conflicts = conflicts;
	}

	public void setReplaces(List<String> replaces) {
		this.replaces = replaces;
	}

	public void setMaxJavaMemory(String maxJavaMemory) {
		this.maxJavaMemory = maxJavaMemory;
	}

	public void setPostinstScript(String postinstScript) {
		this.postinstScript = postinstScript;
	}

	public void setPostrmScript(String postrmScript) {
		this.postrmScript = postrmScript;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPreinstScript(String preinstScript) {
		this.preinstScript = preinstScript;
	}

	public void setPrermScript(String prermScript) {
		this.prermScript = prermScript;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public void setSrcAuxFilesDir(String auxFileSrcDir) {
		this.srcAuxFilesDir = auxFileSrcDir;
	}

	public void setSrcBinFilesDir(String srcBinFilesDir) {
		this.srcBinFilesDir = srcBinFilesDir;
	}

	public void setSrcDataFilesDir(String srcDataFilesDir) {
		this.srcDataFilesDir = srcDataFilesDir;
	}

	public void setSrcDatarootFilesDir(String srcDatarootFilesDir) {
		this.srcDatarootFilesDir = srcDatarootFilesDir;
	}

	public void setSrcIzPackFilesDir(String srcIzPackFilesDir) {
		this.srcIzPackFilesDir = srcIzPackFilesDir;
	}

	public void setSrcJarFilesDir(String srcJarFilesDir) {
		this.srcJarFilesDir = srcJarFilesDir;
	}

	public void setSrcJNIFilesDir(String srcJNIFilesDir) {
		this.srcJNIFilesDir = srcJNIFilesDir;
	}

	public void setSrcSysconfFilesDir(String srcSysconfFilesDir) {
		this.srcSysconfFilesDir = srcSysconfFilesDir;
	}

	public void setSysconfdir(String sysconfdir) {
		this.sysconfdir = sysconfdir;
	}

	public void setSysconfFiles(List<SysconfFile> sysconfFiles) {
		this.sysconfFiles = sysconfFiles;
	}

	public void setSystemProperties(Properties systemProperties) {
		this.systemProperties = systemProperties;
	}

	public void setWrapperScriptName(String wrapperScriptName) {
		this.wrapperScriptName = wrapperScriptName;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendStringDefault(sb, "target", target);
		appendStringDefault(sb, "parent", parent);
		appendStringDefault(sb, "defaultDistro", defaultDistro);
		appendStringDefault(sb, "chosenDistro", chosenDistro);
		
		sb.append("\n");
		appendStringCollection(sb, "distros", distros);
		
		sb.append("\n");
		sb.append("basic packaging options:\n");
		appendStringDefault(sb, "maintainer", maintainer);
		appendStringDefault(sb, "section", section);
		appendStringDefault(sb, "packageNameSuffix", packageNameSuffix);
		appendStringDefault(sb, "architecture", architecture);
		appendStringDefault(sb, "prefix", prefix);
		appendStringDefault(sb, "bindir", bindir);
		appendStringDefault(sb, "sysconfdir", sysconfdir);
		appendStringDefault(sb, "datarootdir", datarootdir);
		appendStringDefault(sb, "datadir", datadir);
		appendStringNotSet(sb, "bundledJarDir", getBundledJarDir());
		appendStringNotSet(sb, "jniLibraryPath", jniLibraryPath);
		appendStringDefault(sb, "izPackInstallerXml", izPackInstallerXml);
		
		sb.append("\n");
		sb.append("packaging scripts:\n");
		appendStringNotSet(sb, "preinstScript", preinstScript);
		appendStringNotSet(sb, "prermScript", prermScript);
		appendStringNotSet(sb, "postinstScript", postinstScript);
		appendStringNotSet(sb, "postrmScript", postrmScript);
		
		sb.append("\n");
		sb.append("packaging flags:\n");
		appendBoolean(sb, "aotCompile", aotCompile);
		appendBoolean(sb, "bundleAll", bundleAll);
		appendBoolean(sb, "advancedStarter", advancedStarter);
		appendBoolean(sb, "sign", sign);
		appendStringDefault(sb, "release", release);
		appendStringDefault(sb, "source", source);
		// TODO rschuster: To my knowledge this is not implemented yet.
//		sb.append("createWindowsExecutable: " + createWindowsExecutable + "\n");
//		sb.append("createOSXApp: " + createOSXApp + "\n");

		sb.append("\n");
		sb.append("dependencies and packaged files:\n");
		appendAuxFileList(sb, "jarFiles", jarFiles);
		appendAuxFileList(sb, "jniFiles", jniFiles);
		
		appendStringCollection(sb, "manualDependencies", manualDependencies);
		appendStringCollection(sb, "bundleDependencies", bundleDependencies);
		appendStringCollection(sb, "recommends", recommends);
		appendStringCollection(sb, "suggests", suggests);
		appendStringCollection(sb, "provides", provides);
		appendStringCollection(sb, "conflicts", conflicts);
		appendStringCollection(sb, "replaces", replaces);
		
		appendAuxFileList(sb, "auxFiles", auxFiles);
		appendAuxFileList(sb, "binFiles", binFiles);
		appendAuxFileList(sb, "datarootFiles", datarootFiles);
		appendAuxFileList(sb, "dataFiles", dataFiles);
		appendAuxFileList(sb, "sysconfFiles", sysconfFiles);
		
		sb.append("\n");
		sb.append("start script options:\n");
		appendStringNotSet(sb, "wrapperScriptName", wrapperScriptName);
		appendStringNotSet(sb, "mainClass", mainClass);
		appendStringNotSet(sb, "maxJavaMemory", maxJavaMemory);
		appendStringNotSet(sb, "customCodeUnix", customCodeUnix);
		appendStringNotSet(sb, "customCodeWindows", customCodeWindows);
		sb.append("systemProperties:\n");
		if (systemProperties != null) {
			Iterator<?> ite = systemProperties.entrySet().iterator();
			while (ite.hasNext())
			{
				sb.append("\t" + ite.next() + "\n");
			}
		} else
			sb.append("\t(not set)\n");

		sb.append("\n");
		sb.append("auxfile locations:\n");
		appendStringDefault(sb, "srcAuxFilesDir", srcAuxFilesDir);
		appendStringDefault(sb, "srcSysconfFilesDir", srcSysconfFilesDir);
		appendStringDefault(sb, "srcBinFilesDir", srcBinFilesDir);
		appendStringDefault(sb, "srcDataFilesDir", srcDataFilesDir);
		appendStringDefault(sb, "srcDatarootFilesDir", srcDatarootFilesDir);
		appendStringDefault(sb, "srcIzPackFilesDir", srcIzPackFilesDir);
		appendStringDefault(sb, "srcJarFilesDir", srcJarFilesDir);
		appendStringDefault(sb, "srcJNIFilesDir", srcJNIFilesDir);
		
		sb.append("\n");
		sb.append("tool locations:\n");
		appendStringDefault(sb, "gcjDbToolExec", gcjDbToolExec);
		appendStringDefault(sb, "gcjExec", gcjExec);

		return sb.toString();
	}
	
	private void appendBoolean(StringBuilder sb, String label, Boolean b)
	{
		sb.append(label);
		sb.append(": ");
		sb.append((b == null || b.equals(Boolean.FALSE) ? "no" : "yes"));
		sb.append("\n");
	}
	
	private void appendStringNotSet(StringBuilder sb, String label, String string)
	{
		sb.append(label);
		sb.append(": ");
		sb.append((string == null ? "(not set)" : string));
		sb.append("\n");
	}
	
	private void appendStringDefault(StringBuilder sb, String label, String string)
	{
		sb.append(label);
		sb.append(": ");
		sb.append((string == null ? "(default)" : string));
		sb.append("\n");
	}
	
	private void appendStringCollection(StringBuilder sb, String label, Collection<?> collection)
	{
		sb.append(label + ":\n");
		if (collection != null && !collection.isEmpty()) {
			Iterator<?> ite = collection.iterator();
			while (ite.hasNext())
			{
				sb.append("\t");
				sb.append(ite.next());
				sb.append("\n");
			}
		} else
			sb.append("\t(not set)\n");
	}
	
	private void appendAuxFileList(StringBuilder sb, String name, List<? extends AuxFile> list)
	{
		sb.append(name + ":\n");
		if (list != null && !list.isEmpty()) {
			Iterator<? extends AuxFile> ite = list.iterator();
			while (ite.hasNext()) {
				AuxFile af = (AuxFile) ite.next();
				sb.append("\t");
				sb.append(af.from);
				sb.append("\n");
				sb.append("\t  ");
				sb.append("-> " + (af.to == null ? "(default dir)" : af.to));
				sb.append("\n");
			}
		} else
			sb.append("\t(not set)\n");

	}

	public boolean isCreateOSXApp() {
		return createOSXApp.booleanValue();
	}

	public void setCreateOSXApp(boolean createOSXApp) {
		this.createOSXApp = Boolean.valueOf(createOSXApp);
	}

	public boolean isCreateWindowsExecutable() {
		return createWindowsExecutable.booleanValue();
	}

	public void setCreateWindowsExecutable(boolean createWindowsExecutable) {
		this.createWindowsExecutable = Boolean.valueOf(createWindowsExecutable);
	}

	public String getCustomCodeUnix() {
		checkIfReady();
		return customCodeUnix;
	}

	public void setCustomCodeUnix(String customCodeUnix) {
		this.customCodeUnix = customCodeUnix;
	}

	public String getCustomCodeWindows() {
		checkIfReady();
		return customCodeWindows;
	}

	public void setCustomCodeWindows(String customCodeWindows) {
		this.customCodeWindows = customCodeWindows;
	}


	public boolean isSign() {
		checkIfReady();
		return sign.booleanValue();
	}


	public void setSign(boolean sign) {
		this.sign = Boolean.valueOf(sign);
	}

	public String getRelease() {
		checkIfReady();
		return release;
	}


	public void setRelease(String release) {
		this.release = release;
	}


	public String getSource() {
		checkIfReady();
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}

	public String getBundledJarDir() {
		checkIfReady();
		return bundledJarDir;
	}


	public void setBundledJarDir(String bundledJarDir) {
		this.bundledJarDir = bundledJarDir;
	}


	public UploadParameters getUploadParameters() {
		checkIfReady();
		return uploadParameters;
	}

	public List<String> getRelations() {
		checkIfReady();
		return relations;
	}


	public void setRelations(List<String> relations) {
		this.relations = relations;
	}

	public void setChosenDistro(String distro) {
		this.chosenDistro=distro;
		
	}


	public void setTarget(String target) {
		this.target=target;		
	}


	public String getDefaultDistro() {
		checkIfReady();
		return defaultDistro;
	}
	
	public void setDefaultDistro(String distro) {
		this.defaultDistro = distro;
	}

	public String getPackageNameSuffix() {
		checkIfReady();
		return packageNameSuffix;
	}

	public void setPackageNameSuffix(String packageNameSuffix) {
		this.packageNameSuffix = packageNameSuffix;
	}

	public String getTarget() {
		return target;
	}

	/**
	 * Returns the configuration with all needed members initialized.
	 * @return
	 */
	public TargetConfiguration fixate() throws MojoExecutionException{
		return Utils.mergeConfigurations(this,new TargetConfiguration());		
	}
	
	/**
	 * Denotes if this configuration is ready to be used 
	 * (i.e. all members have been initialized).<br/>
	 * If false is returned unexpected behaviour may occur. 
	 * @return
	 */
	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	private void checkIfReady() {
		if (!ready)
			throw new IllegalStateException(TargetConfiguration.class.getCanonicalName() + " was not ready to be used. Either call fixate() or merge().");
	}
	
}
