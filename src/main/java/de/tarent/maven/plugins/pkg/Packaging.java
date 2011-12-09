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

import java.util.Iterator;

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


  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    // Maven < 3.0.3 does not accept comma separated values as String[] so we need to split the values ourselves    
    String[] targetArray =  (target != null) ? target.split(",") : new String[]{defaultTarget};	
	
	for(String t : targetArray){
    
		// Generate merged distro configuration.		
	    String d = (distro != null) ? distro : Utils.getDefaultDistro(t,targetConfigurations, getLog());	    
		dc = getMergedConfiguration(t, d, true);
		dc.setChosenTarget(t);
	    
	    // Retrieve package map for chosen distro.
	    pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
	                        dc.bundleDependencies);

	    String packaging = pm.getPackaging();
	    
	    if (packaging == null){
	      throw new MojoExecutionException(
	                                       "Package maps document set no packaging for distro: "
	                                           + dc.getChosenDistro());
	    }
	
	    // Create packager and packaging helper according to the chosen packaging type.	      
	    Helper ph = Utils.getPackagingHelperForPackaging(packaging, dc, this);
	    Packager packager = Utils.getPackagerForPackaging(packaging);
	    
	    if (packager == null){
	      throw new MojoExecutionException("Unsupported packaging type: "+ packaging);
	    }
	  
	    // Store configuration in plugin-context for later use by signer- and deploy-goal
	    getPluginContext().put("dc", dc);
	    getPluginContext().put("pm", pm);
	    getPluginContext().put("packageVersion", ph.getPackageVersion());
	    
	    checkEnvironment(getLog());	
	    packager.checkEnvironment(getLog(), ph);
	    
	    packager.execute(getLog(), ph , pm);
	}
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
    	return new TargetConfiguration().merge(defaults);
    }
    
  }

}
