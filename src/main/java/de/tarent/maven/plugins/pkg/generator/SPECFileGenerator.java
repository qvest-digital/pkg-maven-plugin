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

package de.tarent.maven.plugins.pkg.generator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.RPMFile;

/**
 * Simple generator for SPEC files.
 * 
 * @author Pedro Lafuente (p.lafuente@tarent.de)
 * 
 */
public class SPECFileGenerator {

	private String packageName = "notset";

	private String version = "notset";

	private String description = "notset";

	private String summary = "notset";

	private String license = "notset";

	private String release = "notset";

	private String dependencies;

	private String recommends;

	private String suggests;

	private String source;

	private String url;

	private String group;

	private String arch;

	private List<String> buildcommands = new ArrayList<String>();

	private List<String> installcommands = new ArrayList<String>();

	private List<String> cleancommands = new ArrayList<String>();

	private List<RPMFile> files = new ArrayList<RPMFile>();

	private String vendor;

	private String packager;

	private String prefix;

	private List<String> preparecommands = new ArrayList<String>();
	
	private Log logger;
	public Log getLogger() {
		return logger;
	}

	public void setLogger(Log logger) {
		this.logger = logger;
	}


	private PrintWriter w;

	private List<String> preinstallcommands;

	private List<String> postinstallcommands;

	private List<String> preuninstallcommands;

	private List<String> postuninstallcommands;

	private String buildroot;

	public String getBuildroot() {
		return buildroot;
	}

	public void setBuildroot(String buildroot) {
		this.buildroot = buildroot;
	}

	public SPECFileGenerator() {
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getRecommends() {
		return recommends;
	}

	public void setRecommends(String recommends) {
		this.recommends = recommends;
	}

	public String getSuggests() {
		return suggests;
	}

	public void setSuggests(String suggests) {
		this.suggests = suggests;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public List<String> getBuildcommands() {
		return buildcommands;
	}

	public void setBuildcommands(List<String> setBuildcommands) {
		this.buildcommands = new ArrayList<String>(setBuildcommands);
	}

	public List<String> getInstallcommands() {
		return installcommands;
	}

	public void setInstallcommands(List<String> installcommands) {
		this.installcommands = new ArrayList<String>(installcommands);
	}

	public List<String> getCleancommands() {
		return cleancommands;
	}

	public void setCleancommands(List<String> cleancommands) {
		this.cleancommands = new ArrayList<String>(cleancommands);
	}

	public List<RPMFile> getFiles() {
		return files;
	}

	public void setFiles(List<? extends RPMFile> files) {
		this.files = new ArrayList<RPMFile>(files);
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getPackager() {
		return packager;
	}

	public void setPackager(String packager) {
		this.packager = packager;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public List<String> getPreparecommands() {
		return preparecommands;
	}

	public void setPreparecommands(List<String> preparecommands) {
		this.preparecommands = preparecommands;
	}

	public List<String> getPreinstallcommands() {
		return preinstallcommands;
	}

	public void setPreinstallcommands(List<String> preinstallcommands) {
		this.preinstallcommands = preinstallcommands;
	}

	public List<String> getPostinstallcommands() {
		return postinstallcommands;
	}

	public void setPostinstallcommands(List<String> postinstallcommands) {
		this.postinstallcommands = postinstallcommands;
	}
	
	public void setPreinstallcommandsFromFile(File parent, String filePath) throws IOException {
		setPreinstallcommands(generateArrayListFromExternalFile(parent, filePath));
	}
	
	public void setPostinstallcommandsFromFile(File parent, String filePath) throws IOException {
		setPostinstallcommands(generateArrayListFromExternalFile(parent, filePath));
	}
	
	public List<String> getPreuninstallcommands() {
		return preuninstallcommands;
	}

	public void setPreuninstallcommands(List<String> preuninstallcommands) {
		this.preuninstallcommands = preuninstallcommands;
	}

	
	public void setPreuninstallcommandsFromFile(File parent, String filePath) throws IOException {
			setPreuninstallcommands(generateArrayListFromExternalFile(parent, filePath));

	}
	
	public List<String> getPostuninstallcommands() {
		return postuninstallcommands;
	}

	public void setPostuninstallcommands(List<String> postuninstallcommands) {
		this.postuninstallcommands = postuninstallcommands;
	}
	
	public void setPostuninstallcommandsFromFile(File parent, String filePath)  throws IOException {
		setPostuninstallcommands(generateArrayListFromExternalFile(parent, filePath));
	}
	
	/**
	 * Writes the spec file based on available parameters
	 * @param f
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	public void generate(File f) throws  MojoExecutionException, IOException {
		
		checkneededfields();		
		w = new PrintWriter(new FileOutputStream(f));	
		
		// Make the "all" architecture RPM Compliant
		if (arch == "all") {
			arch = "noarch";
		}
		
		writeEntry("Summary", summary);
		writeEntry("Name", packageName);
		writeEntry("Version", version);
		writeEntry("Release", release);
		writeEntry("Source0", source);
		writeEntry("License", license);
		writeEntry("Group", group);
		writeEntry("URL", url);
		writeEntry("Vendor", vendor);
		writeEntry("Packager", packager);
		writeEntry("BuildArch", arch);
		writeEntry("BuildRoot", buildroot);
		writeEntry("Requires", dependencies);
		writeEntry("Prefix", prefix);

		writeTextSection("description", description);
		writeCommandsSection("prep", preparecommands);
		writeCommandsSection("build", buildcommands);
		writeCommandsSection("install", installcommands);
		writeCommandsSection("clean", cleancommands);
		writeCommandsSection("pre", preinstallcommands);
		writeCommandsSection("post", postinstallcommands);
		writeCommandsSection("preun", preuninstallcommands);
		writeCommandsSection("postun", postuninstallcommands);
		writeFilesSection(files);

		w.close();
	}

	private void checkneededfields() throws MojoExecutionException {
		if(packageName=="notset"||version=="notset"||
				summary=="notset"||description=="notset"||
				license=="notset"||release=="notset"){
			String message = "At least PackageName, Version, Description, Summary License and Release are needed for the spec file.";
			if(logger!=null){
				logger.error(message);
			}
			throw new MojoExecutionException(message);
		}
	}
	/**
	 * Creates a section with the given title and the provided text
	 * @param w
	 * @param sectionTitle
	 * @param text
	 */
	private void writeTextSection(String sectionTitle, String text) {
		if (text!=null && !text.isEmpty()) {
			w.print("%");
			w.println(sectionTitle);
			w.println(text);
		}else{
			if(logger!=null){
				logger.info("Skipping empty text section "+sectionTitle);
			}
		}
	}
	
	/**
	 * Creates a section with the given title, and iterates through the collection,
	 * printing a new line with the contents of the collection. 
	 * @param w
	 * @param sectionTitle
	 * @param commands
	 */
	private void writeCommandsSection(String sectionTitle, List<String> commands) {
		if (commands!=null&&!commands.isEmpty()) {
			w.print("%");
			w.println(sectionTitle);
			for(String s:commands){
				w.println(s);
			}
		}else{
			if(logger!=null){
				logger.info("Skipping empty commands section "+sectionTitle);
			}
		}
	}

	/**
	 * Creates a section with the given title, and iterates through the filelist.
	 * If the file has no rights set, it will default to 755 owner: root, group: root 
	 * @param w
	 * @param files
	 */
	private void writeFilesSection(List<RPMFile> files) {
		if (files!=null&&!files.isEmpty()) {
			w.println("%files");
			w.println("%defattr(755,root,root)");
			
			for (RPMFile f : files) {
				if(f.getOctalPermission()>=0 && f.getOwner()!=null && f.getOwner()!=null){
					w.print("%attr(");
					w.print(f.getOctalPermission());
					w.print(",");
					w.print(f.getOwner());
					w.print(",");
					w.print(f.getGroup());
					w.print(") ");				
				}else{
					if(logger!=null){
						logger.debug("No attributes found for "+f.getAbsolutePath());
					}
				}
				w.println(f.getAbsolutePath());
			}

		}else{
			if(logger!=null){
				logger.warn("Skipping empty files section (Is this a meta-package?)...");
			}
		}
	}
	

	protected void writeEntry(String name, String value) {
		if (value != null)
			w.println(name + ": " + value);
	}	

	/**
	 * Takes the path to a script and turns it into an ArrayList<String>
	 * @param scriptName
	 * @return
	 * @throws MojoExecutionException
	 * @throws IOException 
	 */
	private ArrayList<String> generateArrayListFromExternalFile(File parent, String scriptName) throws IOException{
		
		ArrayList<String> arrayList = new ArrayList<String>();	
		File inputFile = new File(parent, scriptName);
		FileInputStream fstream = new FileInputStream(inputFile);
		DataInputStream in = new DataInputStream(fstream);
		
		BufferedReader inputReader;
		if(logger!=null){
			logger.info("Attempting to process script " + inputFile );
		}
		inputReader = new BufferedReader(new InputStreamReader(in));
			
		String strLine;
		while((strLine = inputReader.readLine()) != null){
			arrayList.add(strLine);
		}
		inputReader.close();
		
		return arrayList;
	}
	

}
