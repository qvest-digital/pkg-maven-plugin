package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates a package file for the project and the given distribution.
 * 
 * @execute phase="package"
 * @goal config
 */
public class ShowConfig extends Packaging
{
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (distroConfigurations == null)
      distroConfigurations = new ArrayList();
    
    getLog().info("default configuration: ");
    
    getLog().info(defaults.toString());
    getLog().info("specific configurations: ");
    
    Iterator ite = distroConfigurations.iterator();
    while (ite.hasNext())
      getLog().info(ite.next().toString());
    
  }
}
