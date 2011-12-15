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
	
	for(String t : getTargets()){
		// A single target (and all its dependent target configurations are supposed to use the same
		// distro value).
	    String d = (distro != null) ? distro : Utils.getDefaultDistro(t, targetConfigurations, getLog());
	    
	    // Retrieve all target configurations that need to be build for /t/
		List<TargetConfiguration> buildChain = Utils.createBuildChain(t, d, targetConfigurations);

		for (TargetConfiguration tc : buildChain) {
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
  
 

}
