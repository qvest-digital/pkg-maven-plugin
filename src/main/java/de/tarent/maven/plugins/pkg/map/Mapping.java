/**
 * 
 */
package de.tarent.maven.plugins.pkg.map;

import java.util.HashMap;

/**
 * A <code>Mapping</code> is the datatype that describes a target
 * distribution (id, label, packaging system, some path names)
 * and the important mapping betwen Maven2 artifacts and the
 * distribution's packages.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
class Mapping
{
  String distro;
  
  String label;
  
  String parent;
  
  String packaging;
  
  String defaultBinPath;
  
  String defaultJarPath;

  String defaultJNIPath;
  
  String defaultDependencyLine;
  
  Boolean debianNaming;
  
  HashMap/*<String, Entry>*/ entries = new HashMap();
  
  /**
   * Creates an empty mapping with the given distro name set.
   * 
   * @param distro
   */
  Mapping(String distro)
  {
    label = this.distro = distro;
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
    packaging = parent.packaging;

    // These values may be null. If the merging has been done from the root to the child
    // they will be non-null for the parent however.
    debianNaming = (child.debianNaming != null) ? child.debianNaming : parent.debianNaming; 
    defaultJarPath = (child.defaultJarPath != null) ? child.defaultJarPath : parent.defaultJarPath; 
    defaultBinPath = (child.defaultBinPath != null) ? child.defaultBinPath : parent.defaultBinPath; 
    defaultJNIPath = (child.defaultJNIPath != null) ? child.defaultJNIPath : parent.defaultJNIPath; 
    defaultDependencyLine = (child.defaultDependencyLine != null) ? child.defaultDependencyLine : parent.defaultDependencyLine;
    
    entries = (HashMap) parent.entries.clone();
    entries.putAll(child.entries);
  }
  
  Entry getEntry(String groupId, String artifactId)
  {
    return (Entry) entries.get(groupId + ":" + artifactId);
  }
  
  void putEntry (String artifactSpec, Entry e)
  {
    entries.put(artifactSpec, e);
  }

}