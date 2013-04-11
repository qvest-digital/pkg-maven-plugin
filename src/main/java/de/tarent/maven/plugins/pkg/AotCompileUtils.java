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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Provides methods to do the ahead of time compilation and all related work
 * conveniently.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 * 
 */
public class AotCompileUtils {

	static String GCJ_EXECUTABLE;
	static String GCJ_DBTOOL_EXECUTABLE;

	public static void setGcjExecutable(String e) {
		GCJ_EXECUTABLE = e;
	}

	public static void setGcjDbToolExecutable(String e) {
		GCJ_DBTOOL_EXECUTABLE = e;
	}

	public static void checkToolAvailability() throws MojoExecutionException {
		Utils.exec(
				new String[] { GCJ_EXECUTABLE, "-dumpversion" },
				GCJ_EXECUTABLE
						+ " returned with an error. Check your installation!",
				GCJ_EXECUTABLE
						+ " is not available on this system. Check your installation!");

		Utils.exec(
				new String[] { GCJ_DBTOOL_EXECUTABLE, "-p" },
				GCJ_DBTOOL_EXECUTABLE
						+ " returned with an error. Check your installation!",
				GCJ_DBTOOL_EXECUTABLE
						+ " is not available on this system. Check your installation!");
	}

	/**
	 * Does a compilation and classmap generation step for a set of artifacts.
	 * It returns the amount of bytes claimed by the classmaps and the aot
	 * binaries.
	 * 
	 * @param l
	 *            A Log instance.
	 * @param artifacts
	 *            A set of Artifact instances.
	 * @param aotDstDir
	 *            The directory where the aot binaries should be put.
	 * @param extension
	 *            The file extension to be appended to each binary (e.g.
	 *            ".jar.so")
	 * @param aotDstClassmapDir
	 *            The directory where the classmaps should be put.
	 * @param overridePath
	 *            The path should be put into the classmap file instead of the
	 *            real file location.
	 * @throws MojoExecutionException
	 */
	public static long compileAndMap(Log l, Set<Artifact> artifacts,
			File aotDstDir, String extension, File aotDstClassmapDir,
			String overridePath) throws MojoExecutionException {
		long byteAmount = 0;

		Iterator<Artifact> ite = artifacts.iterator();
		while (ite.hasNext()) {
			Artifact a = (Artifact) ite.next();
			File src = a.getFile();
			File dst = new File(aotDstDir, a.getArtifactId() + extension);
			File classmap = new File(aotDstClassmapDir, a.getArtifactId()
					+ ".db");
			compile(l, src, dst);
			generateClassmap(l, classmap, src, dst, overridePath);

			byteAmount += classmap.length() + dst.length();
		}

		return byteAmount;
	}

	public static void compile(Log l, File jar, File binary)
			throws MojoExecutionException {
		Utils.createParentDirs(binary, "aot binary");

		l.info("compiling to binary: " + binary.getAbsolutePath());
		Utils.exec(
				new String[] { GCJ_EXECUTABLE, "-O2", "-g", "-Wl,-Bsymbolic",
						"-shared", "-fPIC", "-fjni", "-findirect-dispatch",
						"-o", binary.getAbsolutePath(), jar.getAbsolutePath(), },
				GCJ_EXECUTABLE + " returned with an error.",
				"IOException while doing aot compilation.");

	}

	public static void generateClassmap(Log l, File classmap, File jar,
			File binary, String overridePath) throws MojoExecutionException {
		l.info("creating classmap file with " + GCJ_DBTOOL_EXECUTABLE + ": "
				+ classmap.getAbsolutePath());

		Utils.createParentDirs(classmap, "classmap file");

		Utils.exec(
				new String[] { GCJ_DBTOOL_EXECUTABLE, "-n",
						classmap.getAbsolutePath() }, GCJ_DBTOOL_EXECUTABLE
						+ " returned with an error.",
				"IOException while creating classmap file.");

		String dsoName = (overridePath.length() > 0) ? overridePath + "/"
				+ binary.getName() : binary.getName();

		l.info("filling classmap file");
		Utils.exec(
				new String[] { GCJ_DBTOOL_EXECUTABLE, "-f",
						classmap.getAbsolutePath(), jar.getAbsolutePath(),
						dsoName }, GCJ_DBTOOL_EXECUTABLE
						+ " returned with an error.",
				"IOException while creating classmap file.");

	}

	public static void depositPostinstFile(Log l, File postinstFile)
			throws MojoExecutionException {
		l.info("depositing postinst file for aot-compilation: "
				+ postinstFile.getAbsolutePath());

		Utils.createFile(postinstFile, "postinst");

		// The postinst file does not change and is therefore part of the
		// plugins classpath and can be retrieved from there.
		Utils.storeInputStream(
				AotCompileUtils.class.getResourceAsStream("postinst"),
				postinstFile, "IOException while depositing the postinst file.");

		// Make the postinst file executable.
		Utils.makeExecutable(postinstFile, "postinst");

	}

}
