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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import de.tarent.maven.plugins.pkg.AuxFile;

/**
 * Simple generator for SPEC files.
 * 
 * @author Pedro Lafuente (p.lafuente@tarent.de)
 * 
 */
public class SpecFileGenerator {

	private String packageName = "unknown";

	private String version = "unknown";

	private String description = "uknown";

	private String summary = "unknown";

	private String license = "unknown";

	private String release = "unknown";

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

	private List<AuxFile> files = new ArrayList<AuxFile>();

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

	public SpecFileGenerator() {
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		if (dependencies != null && !dependencies.isEmpty()){
			this.dependencies = dependencies;
		}else{
			if(logger!=null){
				logger.error("dependencies not set");
			}
			this.dependencies = "unknown";
		}
	}

	public String getRecommends() {
		return recommends;
	}

	public void setRecommends(String recommends) {
		if (recommends != null){
			this.description = recommends;
		}else{
			if(logger!=null){
				logger.error("recommends not set");
			}
			this.recommends = "unknown";
		}
	}

	public String getSuggests() {
		return suggests;
	}

	public void setSuggests(String suggests) {
		if (suggests != null){
			this.description = suggests;
		}else{
			if(logger!=null){
				logger.error("suggests not set");
			}
			this.suggests = "unknown";
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description != null){
			this.description = description;
		}else{
			if(logger!=null){
				logger.error("description not set");
			}
			this.description = "unknown";
		}
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		if (packageName != null){
			this.packageName = packageName;
		}else{
			if(logger!=null){
				logger.error("packageName not set");
			}
			this.packageName = "unknown";
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		if (version != null && !version.isEmpty()){
			this.version = version.replace("-", "_");
		}else{
			if(logger!=null){
				logger.error("version not set");
			}
			this.version = "unknown";
		}
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		if (source != null && !source.isEmpty()){
			this.source = source;
		}else{
			if(logger!=null){
				logger.error("source not set");				
			}
			this.source = "unknown";
		}
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		if (summary != null && !summary.isEmpty()){
			this.summary = summary;
		}else{
			if(logger!=null){
				logger.debug("summary not set");
			}
			this.summary = "unknown";
		}
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		if (release != null && !release.isEmpty()){
			this.release = release;
		}else{
			if(logger!=null){
				logger.debug("release not set");
			}
			this.release = "unknown";
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url != null && !url.isEmpty()){
			this.url = url;
		}else{
			if(logger!=null){
				logger.error("url not set");
			}
			this.url = "http://unknown.com";
		}
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		if (license != null&& !license.isEmpty()){
			this.license = license;
		}else{
			if(logger!=null){
				logger.error("license not set");
			}
			this.license = "unknown";
		}
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		if (group != null && !group.isEmpty()){
			this.group = group;
		}else{
			if(logger!=null){
				logger.debug("group not set");
			}
			this.group = "unknown";
		}
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		if (arch != null && !arch.isEmpty()){
			this.arch = arch;
		}else{
			if(logger!=null){
				logger.debug("arch not set");
			}
			this.arch = "noarch";
		}
	}

	public List<String> getBuildcommands() {
		return buildcommands;
	}

	public void setBuildcommands(List<String> setBuildcommands) {		
		if (setBuildcommands != null){
			this.buildcommands = new ArrayList<String>(setBuildcommands);
		}else{
			if(logger!=null){
				logger.debug("buildcommands not set");
			}
			this.buildcommands = new ArrayList<String>();
		}
	}

	public List<String> getInstallcommands() {
		return installcommands;
	}

	public void setInstallcommands(List<String> installcommands) {
		if (installcommands != null){
			this.installcommands = new ArrayList<String>(installcommands);
		}else{
			if(logger!=null){
				logger.debug("installcommands not set");
			}
			this.installcommands = new ArrayList<String>();
		}
	}

	public List<String> getCleancommands() {
		return cleancommands;
	}

	public void setCleancommands(List<String> cleancommands) {
		if (cleancommands != null){
			this.cleancommands = new ArrayList<String>(cleancommands);
		}else{
			if(logger!=null){
				logger.debug("cleancommands not set");
			}
			this.cleancommands = new ArrayList<String>();
		}
	}

	public List<AuxFile> getFiles() {
		return files;
	}

	public void setFiles(List<? extends AuxFile> files) {
		if (files != null){
			this.files = new ArrayList<AuxFile>(files);
		}else{
			if(logger!=null){
				logger.debug("files not set");
			}
			this.files = new ArrayList<AuxFile>();
		}
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		if (vendor != null){
			this.vendor = vendor;
		}else{
			if(logger!=null){
				logger.debug("vendor not set");
			}
			this.vendor = "unknown";
		}
	}

	public String getPackager() {
		return packager;
	}

	public void setPackager(String packager) {
		if (packager != null){
			this.packager = packager;
		}else{
			if(logger!=null){
				logger.debug("packager not set");
			}
			this.packager = "unknown";
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		if (prefix != null){
			this.prefix = prefix;
		}else{
			if(logger!=null){
				logger.debug("prefix not set");
			}
			this.prefix = "/";
		}
	}


	public List<String> getPreparecommands() {
		return preparecommands;
	}

	public void setPreparecommands(List<String> preparecommands) {
		if (preparecommands != null){
			this.preparecommands = preparecommands;
		}else{
			if(logger!=null){
				logger.debug("preparecommands not set");
			}
			this.preparecommands = new ArrayList<String>();
		}
	}

	public List<String> getPreinstallcommands() {
		return preinstallcommands;
	}

	public void setPreinstallcommands(List<String> preinstallcommands) {
		if (preinstallcommands != null){
			this.preinstallcommands = preinstallcommands;
		}else{
			if(logger!=null){
				logger.debug("preinstallcommands not set");
			}
			this.preinstallcommands = new ArrayList<String>();
		}
	}

	public List<String> getPostinstallcommands() {
		return postinstallcommands;
	}

	public void setPostinstallcommands(List<String> postinstallcommands) {
		if (postinstallcommands != null){
			this.postinstallcommands = postinstallcommands;
		}else{
			if(logger!=null){
				logger.debug("postinstallcommands not set");
			}
			this.postinstallcommands = new ArrayList<String>();
		}
	}
	
	public void setPreinstallcommandsFromFile(File parent, String fileName) throws IOException {
		if(parent != null && fileName != null && parent.exists() && !fileName.isEmpty()){
			setPreinstallcommands(generateArrayListFromExternalFile(parent, fileName));
		}else{
			if(logger!=null){
				logger.debug("Error setting preinstall commands.");
			}
			this.preinstallcommands = new ArrayList<String>();
		}
	}
	
	public void setPostinstallcommandsFromFile(File parent, String filePath) throws IOException {
		if(parent != null && filePath != null && parent.exists() && !filePath.isEmpty()){
			setPostinstallcommands(generateArrayListFromExternalFile(parent, filePath));
		}else{
			if(logger!=null){
				logger.debug("Error setting postinstall commands.");
			}
			this.postinstallcommands = new ArrayList<String>();
		}
	}
	
	public List<String> getPreuninstallcommands() {
		return preuninstallcommands;
	}

	public void setPreuninstallcommands(List<String> preuninstallcommands) {
		if (preuninstallcommands != null){
			this.preuninstallcommands = preuninstallcommands;
		}else{
			if(logger!=null){
				logger.debug("preuninstallcommands not set");
			}
			this.preuninstallcommands = new ArrayList<String>();
		}
	}

	
	public void setPreuninstallcommandsFromFile(File parent, String filePath) throws IOException {
		if(parent != null && filePath != null && parent.exists() && !filePath.isEmpty()){
			setPreuninstallcommands(generateArrayListFromExternalFile(parent, filePath));
		}else{
			if(logger!=null){
				logger.debug("Error setting preuninstall commands.");
			}
			this.preuninstallcommands = new ArrayList<String>();
		}

	}
	
	public List<String> getPostuninstallcommands() {
		return postuninstallcommands;
	}

	public void setPostuninstallcommands(List<String> postuninstallcommands) {
		if (postuninstallcommands != null){
			this.postuninstallcommands = postuninstallcommands;
		}else{
			if(logger!=null){
				logger.debug("postuninstallcommands not set");
			}
			this.postuninstallcommands = new ArrayList<String>();
		}
	}
	
	public void setPostuninstallcommandsFromFile(File parent, String filePath)  throws IOException {
		
		if(parent != null && filePath != null && parent.exists() && !filePath.isEmpty()){
			setPostuninstallcommands(generateArrayListFromExternalFile(parent, filePath));
		}else{
			if(logger!=null){
				logger.debug("Error setting postuninstall commands.");
			}
			this.postuninstallcommands = new ArrayList<String>();
		}
	}
	
	/**
	 * Writes the spec file based on available parameters
	 * @param f
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	public void generate(File f) throws  MojoExecutionException, IOException {
				
		w = new PrintWriter(new FileOutputStream(f));
		
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
	private void writeFilesSection(List<AuxFile> files) {
		if (files!=null&&!files.isEmpty()) {
			w.println("%files");
			w.println("%defattr(755,root,root)");
			
			for (AuxFile f : files) {
				if(f.getOctalPermission()>=0){
					w.print("%attr(");
					w.print(f.getOctalPermission());
					w.print(",");
					w.print(f.getOwner());
					w.print(",");
					w.print(f.getGroup());
					w.print(") ");				
				}else{
					if(logger!=null){
						logger.debug("No attributes found for "+f.getTo());
					}
				}
				w.println(f.getTo());
			}

		}else{
			if(logger!=null){
				logger.debug("Skipping empty files section (Is this a meta-package?)...");
			}
		}
	}
	

	protected void writeEntry(String name, String value) {
		if (value != null){
			if (logger!=null){
				logger.debug(name + ": " + value);
			}
			w.println(name + ": " + value);
		}else{
			if (logger!=null){
			logger.debug(name + " not set!");
			}
		}
	}	

	/**
	 * Takes the path to a script and turns it into an ArrayList<String>
	 * @param scriptName
	 * @return
	 * @throws MojoExecutionException
	 * @throws IOException 
	 */
	private List<String> generateArrayListFromExternalFile(File parent, String scriptName) throws IOException{
		
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
