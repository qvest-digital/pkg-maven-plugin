  /**
 * 
 */
package de.tarent.maven.plugins.pkg.map;

import java.util.HashSet;

/**
 * An <code>Entry<code> instance denotes a single mapping between a Maven2 artifact
 * and a package in the target distribution.
 * 
 * <p>It gives information on the package name, the Jar files which belong to it
 * and whether it should be part of the classpath or boot classpath.</p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
public class Entry
{
  /**
   * Special instance that denotes an entry that should be ignored for packaging.
   */
  static final Entry IGNORE_ENTRY = new Entry();

  /**
   * Special instance that denotes an entry that should be bundled with the
   * project.
   */
  static final Entry BUNDLE_ENTRY = new Entry();

  public String artifactId;
  
  public String packageName;
  
  public HashSet jarFileNames;
  
  public boolean isBootClasspath;

  private Entry()
  {
    // For internal instances only.
  }

  Entry(String artifactId, String packageName, HashSet jarFileNames, boolean isBootClasspath)
  {
    this.artifactId = artifactId;
    this.packageName = packageName;
    this.jarFileNames = jarFileNames;
    this.isBootClasspath = isBootClasspath;
  }
}