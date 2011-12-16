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
import java.util.Map;

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
  
  /**
   * Creates the package for a single given target configuration.
   * 
   * @param tc
   * @param d
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
	@Override
	protected void executeTargetConfiguration(WorkspaceSession ws, String d)
			throws MojoExecutionException, MojoFailureException {
	    AbstractPackagingMojo mojo = ws.getMojo();
	    TargetConfiguration tc = ws.getTargetConfiguration();
	    Map<String, TargetConfiguration> tcMap = ws.getTargetConfigurationMap();
	    
	    // At first we create the various work objects that we need to process the
	    // request to package what is specified in 'tc' and test their validity.

	    // Resolve all the relations of the given target configuration. This can fail
	    // with an exception if there is a configuration mistake.
	    List<TargetConfiguration> resolvedRelations = Utils.resolveConfigurations(
	    		tc.getRelations(), tcMap);
		
	    // Retrieve package map for chosen distro.
	    PackageMap pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
	                        tc.getBundleDependencies());

	    String packaging = pm.getPackaging();
	    
	    if (packaging == null){
	      throw new MojoExecutionException(
	                                       "Package maps document set no packaging for distro: "
	                                           + tc.getChosenDistro());
	    }
	
	    // Create packager and packaging helper according to the chosen packaging type.
	    // Note: Helper is historically strongly dependent on the mojo, the package and
	    // the targetconfiguration because this makes method calls to the helper so neatly
	    // short and uniform among all Packager implementations, ie. there're almost no
	    // arguments needed and all packagers call the same stuff while in reality they're
	    // subtle differences between them.
	    Helper ph = Utils.getPackagingHelperForPackaging(packaging);
	    ph.init(mojo, pm, tc, resolvedRelations);
	    
	    Packager packager = Utils.getPackagerForPackaging(packaging);
	    
	    if (packager == null){
	      throw new MojoExecutionException("Unsupported packaging type: "+ packaging);
	    }
	  
	    // Store configuration in plugin-context for later use by signer- and deploy-goal
	    // TODO: This is completely broken now because a single run of the plugin can
	    // create multiple binary packages and these variables assume that there is just one.
	    // TODO: This stuff makes my eyes bleed.
	    getPluginContext().put("dc", tc);
	    getPluginContext().put("pm", pm);
	    getPluginContext().put("packageVersion", ph.getPackageVersion());
	    
	    // Finally now that we know that our cool newly created work objects are
	    // prepared and can be used (none of them is null) we stuff them 
	    // into the session and run the actual packaging steps.
	    ws.setResolvedRelations(resolvedRelations);
	    ws.setPackageMap(pm);
	    ws.setHelper(ph);
	    ws.setPackager(packager);
	    
	    packager.checkEnvironment(getLog(), ws);
	    
	    packager.execute(getLog(), ws);
  }
  
 

}
