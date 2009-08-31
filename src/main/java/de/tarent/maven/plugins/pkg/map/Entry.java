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

  /**
 * 
 */
package de.tarent.maven.plugins.pkg.map;

import java.util.HashSet;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.maven.artifact.versioning.VersionRange;

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
  
  public VersionRange versionRange;

  private Entry()
  {
    // For internal instances only.
  }

  Entry(String artifactId, VersionRange versionRange, String packageName, HashSet jarFileNames, boolean isBootClasspath)
  {
    this.artifactId = artifactId;
    this.versionRange = versionRange;
    this.packageName = packageName;
    this.jarFileNames = jarFileNames;
    this.isBootClasspath = isBootClasspath;
  }
  
  public boolean equals(Object o)
  {
	  if (o instanceof Entry)
	  {
		Entry that = (Entry) o;
		return this.artifactId.equals(that.artifactId)
			&& this.packageName.equals(that.packageName)
			&& this.jarFileNames.equals(that.jarFileNames)
			&& this.isBootClasspath == that.isBootClasspath
			&& this.versionRange.equals(that.versionRange);
	  }
	  
	  return false;
  }
  
  public int hashCode()
  {
	  HashCodeBuilder hb = new HashCodeBuilder();
	  hb.append(artifactId)
	  	.append(packageName)
	  	.append(jarFileNames)
	  	.append(isBootClasspath)
	  	.append(versionRange);
	  
	  return hb.toHashCode();
  }
}