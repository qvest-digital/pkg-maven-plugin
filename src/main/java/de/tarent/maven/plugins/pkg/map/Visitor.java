/**
 * 
 */
package de.tarent.maven.plugins.pkg.map;

import org.apache.maven.artifact.Artifact;

/**
 * Small interface that is used as an argument for the
 * {@link PackageMap#iterateDependencyArtifacts(org.apache.maven.plugin.logging.Log, java.util.Collection, Visitor, boolean)
 * method.
 * 
 * <p>The method iterates through the dependencies and calls the interface's
 * two methods accordingly.</p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
public interface Visitor
{
  /**
   * If called it denotes that this is a normal dependency which will be
   * provided through the target system's package management.
   * 
   * @param artifact
   * @param entry
   */
  public void visit(Artifact artifact, Entry entry);
  
  /**
   *  If called it means that the dependency will be bundled along
   *  with the application.
   *  
   * @param artifact
   */
  public void bundle(Artifact artifact);
}