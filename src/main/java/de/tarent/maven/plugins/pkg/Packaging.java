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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;
import de.tarent.maven.plugins.pkg.packager.Packager;

/**
 * Creates a package file for the project and the given distribution.
 * 
 * @execute phase="package"
 * @goal pkg
 */
public class Packaging
    extends AbstractPackagingMojo
{

  public void execute() throws MojoExecutionException, MojoFailureException
  {
	// Container for collecting target configurations that have been built. This
	// is used to make sure that TCs are not build repeatedly when the given target
	// configuration have a dependency to a common target configuration.
	HashSet<String> finishedTargets = new HashSet<String>();

    // Maven < 3.0.3 does not accept comma separated values as String[] so we need to split the values ourselves    
    String[] targetArray =  (target != null) ? target.split(",") : new String[]{defaultTarget};	
	
	for(String t : targetArray){
		// A single target (and all its dependent target configurations are supposed to use the same
		// distro value).
	    String d = (distro != null) ? distro : Utils.getDefaultDistro(t,targetConfigurations, getLog());
	    
	    // Retrieve all target configurations that need to be build for /t/
		List<TargetConfiguration> targetConfigurations = getMergedConfigurations(t, d);

		for (TargetConfiguration tc : targetConfigurations) {
			if (!finishedTargets.contains(tc.getTarget()))
			{
				executeTargetConfiguration(tc, d);

				// Mark as done.
			    finishedTargets.add(tc.getTarget());
			}

		}
	}
  }
  
  /**
   * Creates the package for a single given target configuration.
   * 
   * @param tc
   * @param d
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  private void executeTargetConfiguration(TargetConfiguration tc, String d) throws MojoExecutionException, MojoFailureException {
	    
	    // Retrieve package map for chosen distro.
	    pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
	                        tc.bundleDependencies);

	    String packaging = pm.getPackaging();
	    
	    if (packaging == null){
	      throw new MojoExecutionException(
	                                       "Package maps document set no packaging for distro: "
	                                           + tc.getChosenDistro());
	    }
	
	    // Create packager and packaging helper according to the chosen packaging type.	      
	    Helper ph = Utils.getPackagingHelperForPackaging(packaging, tc, this);
	    Packager packager = Utils.getPackagerForPackaging(packaging);
	    
	    if (packager == null){
	      throw new MojoExecutionException("Unsupported packaging type: "+ packaging);
	    }
	  
	    // Store configuration in plugin-context for later use by signer- and deploy-goal
	    // TODO: This is completely broken now because a single run of the plugin can
	    // create multiple binary packages and these variables assume that there is just one.
	    getPluginContext().put("dc", tc);
	    getPluginContext().put("pm", pm);
	    getPluginContext().put("packageVersion", ph.getPackageVersion());
	    
	    packager.checkEnvironment(getLog(), ph);
	    
	    packager.execute(getLog(), ph , pm);
  }
  
  private List<TargetConfiguration> getMergedConfigurations(String target, String distro)
  	  throws MojoExecutionException
  {
	  LinkedList<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
	  
	  TargetConfiguration tc = getMergedConfiguration(target, distro, true);
	  tcs.addFirst(tc);
	  
	  List<String> relations = tc.getRelations();
	  for (String relation : relations) {
		  tcs.addAll(0, getMergedConfigurations(relation, distro));
	  }
	  
	  return tcs;
  }
  
  /**
   * Takes the default configuration and the custom one into account and creates
   * a merged one.
   * 
   * @param target Chosen target configuration
   * @param distro Chosen distro
   * @param mustMatch Whether a result must be found or whether it is OK to rely on defaults.
   * @return
   */
  private TargetConfiguration getMergedConfiguration(String target, String distro, boolean mustMatch)
      throws MojoExecutionException
  {
    Iterator<TargetConfiguration> ite = targetConfigurations.iterator();
    while (ite.hasNext())
      {
        TargetConfiguration currentTargetConfiguration = ite.next();
        
        // The target configuration should be for the requested target.
        if (!currentTargetConfiguration.getTarget().equals(target))
        	continue;
        
        // Recursively creates the merged configuration of the parent. By doing so we
        // traverse the chain of configurations from the bottom to the top.
        TargetConfiguration merged = getMergedConfiguration(currentTargetConfiguration.parent, distro, false);

        // Checks whether this targetconfiguration supports
        // the wanted distro.
        if (currentTargetConfiguration.getDistros().contains(distro) || merged.getDistros().contains(distro))
          {
            // Stores the chosen distro in the configuration for later use.
            currentTargetConfiguration.setChosenDistro(distro);

            // Returns a configuration that is merged with
            // the default configuration-
            return currentTargetConfiguration.merge(merged);
          }
      }
    
    // For the target the user requested a result must be found (first case) but when the
    // plugin looks up parent configuration it will finally reach the default configuration
    // and for this it is necessary to derive from it without a match.
    if (mustMatch)
    {
    	throw new MojoExecutionException("Requested target " + target + " does not exist. Check spelling or configuration.");
    }
    else
    {
    	return new TargetConfiguration(target).merge(defaults);
    }
    
  }

}
