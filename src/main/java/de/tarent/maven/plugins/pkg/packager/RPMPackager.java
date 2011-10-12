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

/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb and izpack)
 * Copyright (C) 2000-2007 tarent GmbH
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
 * Signature of Elmar Geese, 14 June 2007
 * Elmar Geese, CEO tarent GmbH.
 */

package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.IPackagingHelper;
import de.tarent.maven.plugins.pkg.Packaging;
import de.tarent.maven.plugins.pkg.Packaging.RPMHelper;
import de.tarent.maven.plugins.pkg.RPMFile;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.generator.SPECFileGenerator;
import de.tarent.maven.plugins.pkg.map.PackageMap;

/**
 * Creates a RPM package file
 * 
 * @author plafue
 */
public class RPMPackager extends Packager {

	@Override
	public void execute(Log l, IPackagingHelper helper,
			TargetConfiguration distroConfig, PackageMap packageMap)
			throws MojoExecutionException {	
		
		if(!(helper instanceof RPMHelper)){
			throw new IllegalArgumentException("RPMHelper needed");
		}
		
		RPMHelper ph = (RPMHelper) helper;
		ph.prepareInitialDirectories();
		ph.copyFilesAndSetFileList();

		l.debug(ph.getPackageName());
		l.debug(ph.getPackageVersion());
		l.debug(ph.getBasePkgDir().getPath());

		File specFile = new File(ph.getBaseSpecsDir(), ph.getPackageName() + ".spec");

		try {
			generateSPECFile(l, (RPMHelper) ph, distroConfig, specFile);
			createPackage(l, ph, specFile, distroConfig);
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.toString());
		} finally {
			try {
				ph.restorerpmmacrosfile(l);
			} catch (IOException e) {
				throw new MojoExecutionException(e.toString());
			}
		}
	}


	/**
	 * Will prepare the custom Build Area by creating the .rpmmacrosfile
	 */
	@Override
	public void checkEnvironment(Log l, IPackagingHelper helper,
			TargetConfiguration dc) throws MojoExecutionException {
		RPMHelper ph = (RPMHelper)helper;
		try {
			ph.createrpmmacrosfile(l, ph, dc);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

	/**
	 * Takes the parameters inside Packaging.Helper and generates the
	 * spec file needed for rpmbuild to work.
	 * 
	 * @param l
	 * @param ph
	 * @param dc
	 * @param specFile
	 * @throws MojoExecutionException
	 * @throws IOException 
	 */
	private void generateSPECFile(Log l, RPMHelper ph, TargetConfiguration dc,
			File specFile) throws MojoExecutionException, IOException {

		SPECFileGenerator sgen = new SPECFileGenerator();
		// TODO: Make this configurable through pom
		sgen.setBuildroot("%{_builddir}");
		
		sgen.setLogger(l);
		
		// Following parameters MUST be provided for rpmbuild to work:
		sgen.setPackageName(ph.getPackageName());
		sgen.setVersion(ph.getVersion());
		sgen.setSummary(ph.getProjectDescription());
		sgen.setDescription(ph.getProjectDescription());
		sgen.setLicense(dc.getLicense());
		sgen.setRelease(dc.getRelease());
		sgen.setSource(dc.getSource());
		
		// Following parameters are not mandatory
		sgen.setArch(dc.getArchitecture());
		sgen.setPrefix(dc.getPrefix());
		sgen.setPackager(dc.getMaintainer());
		sgen.setFiles(ph.getFilelist());

		sgen.setPreinstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPreinstScript());
		sgen.setPostinstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPostinstScript());
		sgen.setPreuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPrermScript());
		sgen.setPostuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),dc.getPostrmScript());

		l.info("Creating SPEC file: " + specFile.getAbsolutePath());
		Utils.createFile(specFile, "spec");

		try {
			sgen.generate(specFile);
		} catch (IOException ioe) {
			throw new MojoExecutionException(
					"IOException while creating SPEC file.", ioe);
		} catch (MojoExecutionException e) {
			throw e;
		}

	}

	/**
	 * Executes rpmbuild, that will generate the final rpm package.
	 * 
	 * If the parameter "sign" is set as true in pom, package will 
	 * be signed with the Maintainer (Packager) name provided.
	 * 
	 * @param l
	 * @param ph
	 * @param specFile
	 * @throws MojoExecutionException
	 */
	private void createPackage(Log l, RPMHelper ph, File specFile,
			TargetConfiguration dc) throws MojoExecutionException {
		l.info("Calling rpmbuild to create binary package");
		l.info("Builddir is "+ph.getBaseBuildDir().toString());
		String[] command;
		if (dc.isSign()) {
			command = new String[] { "rpmbuild", "-bb", "--sign",
					"--buildroot", ph.getBaseBuildDir().toString(),
					specFile.toString() };

		} else {
			command = new String[] { "rpmbuild", "-bb", "--buildroot",
					ph.getBaseBuildDir().toString(),
					specFile.toString() };
		}

		Utils.exec(command, "'rpmbuild -bb' failed.",
				"Error creating rpm file.");
	}
}
