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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.tarent.maven.plugins.pkg.packager.Packager;

/**
 * Creates a package file for the project and the given distribution.
 * 
 * @phase package
 * @goal pkg
 * @requiresProject
 * @requiresDependencyResolution runtime
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
	    Packager packager = Utils.getPackagerForPackaging(ws.getPackageMap().getPackaging());
	    
	    // Finally now that we know that our cool newly created work objects are
	    // prepared and can be used (none of them is null) we stuff them 
	    // into the session and run the actual packaging steps.
	    ws.setPackager(packager);
	    
	    packager.checkEnvironment(getLog(), ws);
	    
	    packager.execute(getLog(), ws);
  }
  
 

}
