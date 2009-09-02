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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * A <code>Mapping</code> is the datatype that describes a target
 * distribution (id, label, packaging system, some path names)
 * and the important mapping between Maven2 artifacts and the
 * distribution's packages.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
class Mapping
{
  String distro;
  
  String label;
  
  String repoName;
  
  String parent;
  
  String packaging;
  
  String defaultBinPath;
  
  String defaultJarPath;

  String defaultJNIPath;
  
  String defaultDependencyLine;
  
  Boolean debianNaming;
  
  boolean hasNoPackages;
  
  HashMap<String, HashSet<Entry>> entryMap = new HashMap<String, HashSet<Entry>>();
  
  /**
   * Creates an empty mapping with the given distro name set.
   * 
   * @param distro
   */
  Mapping(String distro)
  {
    this.distro = distro;
  }
  
  /**
   * Creates a combination of the given child and parent mapping.
   * 
   * All properties from the child that are set are taken. The others
   * are taken from the parent.
   * 
   * In case of the mapping itself: The entries from the parent are
   * cloned and the ones from the child are added to it (possibly replacing
   * existing entries).
   * 
   * @param child
   * @param parent
   */
  Mapping (Mapping child, Mapping parent)
  {
    distro = child.distro;
    this.parent = parent.distro;
    packaging = parent.packaging;
    
    // Whether packages exist or not is always inherit from the parent.
    hasNoPackages = parent.hasNoPackages;

    // These values may be null. If the merging has been done from the root to the child
    // they will be non-null for the parent however.
    label = (child.label != null) ? child.label : parent.label; 
    packaging = (child.packaging != null) ? child.packaging : parent.packaging; 
    repoName = (child.repoName != null) ? child.repoName : parent.repoName;
    debianNaming = (child.debianNaming != null) ? child.debianNaming : parent.debianNaming; 
    defaultJarPath = (child.defaultJarPath != null) ? child.defaultJarPath : parent.defaultJarPath; 
    defaultBinPath = (child.defaultBinPath != null) ? child.defaultBinPath : parent.defaultBinPath; 
    defaultJNIPath = (child.defaultJNIPath != null) ? child.defaultJNIPath : parent.defaultJNIPath; 
    defaultDependencyLine = (child.defaultDependencyLine != null) ? child.defaultDependencyLine : parent.defaultDependencyLine;
    
    // Make a deep copy of the entry map contents. Otherwise parent and 'this' would
    // share the HashSet<Entry> instance causing damaging effects to the parent when stuff
    // is changed in the child.
    for (Map.Entry<String, HashSet<Entry>> e : parent.entryMap.entrySet())
    {
    	HashSet<Entry> set = new HashSet<Entry>();
    	set.addAll(e.getValue());
    	entryMap.put(e.getKey(), set);
    }
    
    for (Map.Entry<String, HashSet<Entry>> e : child.entryMap.entrySet())
    {
    	HashSet<Entry> set = entryMap.get(e.getKey());
    	if (set == null)
    	{
    		set = new HashSet<Entry>();
    		entryMap.put(e.getKey(), set);
    	}
    	
    	// This part is tricky. We want to achieve that Entry instances from
    	// 'tset' (the child's Entry instances) replace those that are equivalent to
    	// those in 'set' (the ones inherited from parent). We assume that the child's
    	// instances are newer.
    	// Entry is coded in a way that only the artifactSpec and versionRange properties
    	// are needed to decide whether two instances are equal (same goes for hashCode()).
    	//
    	// As such we do a remove operation first, which will delete the equivalent instances.
    	// Afterwards we add the whole set from child.
    	//
    	// The addAll() implementation alone would not *replace* the existing instances.
    	HashSet<Entry> tset = e.getValue();
    	set.removeAll(tset);
		set.addAll(tset);
		
    }
        
  }
  
  Entry getEntry(String groupId, String artifactId, ArtifactVersion artifactVersion)
  {
	HashSet<Entry> entryList = entryMap.get(groupId + ":" + artifactId);
    if (entryList == null)
    	return null;
    
    Entry unrangedCandidate = null;
    
    for (Entry e : entryList)
    {
    	if (e.versionRange != null)
    	{
    		if (e.versionRange.containsVersion(artifactVersion))
    			return e;
    	}
    	else
    	{
    		unrangedCandidate = e;
    	}
    }
    
    return unrangedCandidate;
  }
  
  void putEntry (Entry e)
  {
	  HashSet<Entry> list = entryMap.get(e.artifactSpec);
	  if (list == null)
	  {
		  entryMap.put(e.artifactSpec, list = new HashSet<Entry>());
	  }
	  
	  list.add(e);
  }

}