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

package de.tarent.maven.plugins.pkg.packager;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class IzPackDescriptor {
	private static final String AOT_PACK_ID = "aot_pack";

	private static final String ROOT_INSTALL_PACK_ID = "root_install_pack";

	private static final String APP_PACK_ID = "app_pack";

	Document doc;

	IzPackDescriptor(File source, String exceptionMsg)
			throws MojoExecutionException {
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(source);
		} catch (SAXException e) {
			throw new MojoExecutionException(exceptionMsg, e);
		} catch (IOException e) {
			throw new MojoExecutionException(exceptionMsg, e);
		} catch (ParserConfigurationException e) {
			throw new MojoExecutionException(exceptionMsg, e);
		}
	}

	void finish(File dest, String exceptionMsg) throws MojoExecutionException {
		Source source = new DOMSource(doc);
		StreamResult result = new StreamResult(dest);

		try {
			TransformerFactory.newInstance().newTransformer()
					.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new MojoExecutionException(exceptionMsg, e);
		} catch (TransformerException e) {
			throw new MojoExecutionException(exceptionMsg, e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new MojoExecutionException(exceptionMsg, e);
		}
	}

	void fillInfo(Log l, String appName, String appVersion, String url) {
		Node infoNode = doc.getElementsByTagName("info").item(0);

		if (!childExists(infoNode, "appname")) {
			l.info("setting <appname> to: " + appName);
			Node n = doc.createElement("appname");
			n.setTextContent(appName);
			infoNode.appendChild(n);
		}

		if (!childExists(infoNode, "appversion")) {
			l.info("setting <appversion> to: " + appVersion);
			Node n = doc.createElement("appversion");
			n.setTextContent(appVersion);
			infoNode.appendChild(n);
		}

		if (!childExists(infoNode, "appsubpath")) {
			l.info("setting <appsubpath> to: " + appName);
			Node n = doc.createElement("appsubpath");
			n.setTextContent(appName);
			infoNode.appendChild(n);
		}

		if (!childExists(infoNode, "url")) {
			if (url == null) {
				l.warn("Neither the IzPack descriptor nor the POM contain a project URL!");
				return;
			}
			l.info("setting <url> to: " + url);
			Node n = doc.createElement("url");
			n.setTextContent(url);
			infoNode.appendChild(n);
		}
	}

	private Node getAppPackNode() throws MojoExecutionException {
		Node appPackNode = doc.getElementById(APP_PACK_ID);
		NamedNodeMap attrs;

		if (appPackNode == null) {
			// Add the wrapper script to the first required pack.
			NodeList packs = doc.getElementsByTagName("pack");

			int size = packs.getLength();
			for (int i = 0; i < size; i++) {
				if (packs.item(i).getAttributes().getNamedItem("required")
						.getNodeValue().equals("yes")) {
					appPackNode = packs.item(i);
					break;
				}
			}
		} else {
			attrs = appPackNode.getAttributes();
			attrs.setNamedItem(createAttribute("required", "yes"));

			if (!attributeExists(attrs, "name")) {
				attrs.setNamedItem(createAttribute("name",
						"base installation files"));
			}
		}

		if (appPackNode == null) {
			throw new MojoExecutionException(
					"Unable to find a <pack> which is mandatory (where 'required' equals 'yes')");
		}
		return appPackNode;
	}

	/**
	 * Adds the information to let IzPack install a Unix wrapper script. The
	 * basic installation information should be added to the pack which denotes
	 * the application' base installation. In order to find it a node with the
	 * XML id identical to <code>APP_PACK_ID</code> is searched. If it can be
	 * found its required parameter is set to "yes". If it cannot be found the
	 * method searches for the first node whose "required" attribute is set to
	 * "yes". If the search fails a <code>MojoExecutionException</code> is
	 * thrown.
	 * 
	 * Additionally another pack is created which allows installing the start
	 * script to a system-wide location. If the XML contains a pack whose XML id
	 * is identical to <code>ROOT_INSTALL_PACK_ID</code> this method will use
	 * that pack only adds missing information.
	 * 
	 * The root install pack is supposed to have a "name", "required",
	 * "preselected" and "os" attribute. If any of those is given, it will not
	 * be overwritten. Missing entries will be added with default values.
	 * 
	 * The root install pack is supposed to have a description, file, parsable
	 * and executable child node. Any existing child node is not touched and
	 * missing elements are added automatically.
	 * 
	 * @param scriptName
	 * @param appDescription
	 * @throws MojoExecutionException
	 */
	void addUnixWrapperScript(String scriptName, String appDescription)
			throws MojoExecutionException {
		Node appPackNode = getAppPackNode();
		NamedNodeMap attrs;

		Node descriptionNode = doc.createElement("description");
		descriptionNode.setTextContent(appDescription);
		appPackNode.appendChild(descriptionNode);

		Node fileNode = doc.createElement("file");
		attrs = fileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", scriptName));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		attrs.setNamedItem(createAttribute("os", "unix"));
		appPackNode.appendChild(fileNode);

		Node parsableNode = doc.createElement("parsable");
		attrs = parsableNode.getAttributes();
		attrs.setNamedItem(createAttribute("type", "shell"));
		attrs.setNamedItem(createAttribute("targetfile", "$INSTALL_PATH/"
				+ scriptName));
		attrs.setNamedItem(createAttribute("os", "unix"));
		appPackNode.appendChild(parsableNode);

		Node executableNode = doc.createElement("executable");
		attrs = executableNode.getAttributes();
		attrs.setNamedItem(createAttribute("targetfile", "$INSTALL_PATH/"
				+ scriptName));
		attrs.setNamedItem(createAttribute("keep", "true"));
		attrs.setNamedItem(createAttribute("stage", "never"));
		attrs.setNamedItem(createAttribute("os", "unix"));
		appPackNode.appendChild(executableNode);

		// Add another pack which allows to install the wrapper script in a
		// public (eg. /usr/local/bin) location.

		Node packsNode = doc.getElementsByTagName("packs").item(0);
		Node rootInstallPackNode;

		rootInstallPackNode = doc.getElementById(ROOT_INSTALL_PACK_ID);
		if (rootInstallPackNode == null) {
			rootInstallPackNode = doc.createElement("pack");
		}
		attrs = rootInstallPackNode.getAttributes();
		if (!attributeExists(attrs, "name")) {
			attrs.setNamedItem(createAttribute("name",
					"Wrapper script in PATH (requires root privileges!)"));
		}
		if (!attributeExists(attrs, "preselected")) {
			attrs.setNamedItem(createAttribute("preselected", "no"));
		}
		if (!attributeExists(attrs, "required")) {
			attrs.setNamedItem(createAttribute("required", "no"));
		}
		if (!attributeExists(attrs, "os")) {
			attrs.setNamedItem(createAttribute("os", "unix"));
		}
		packsNode.appendChild(rootInstallPackNode);

		if (!childExists(rootInstallPackNode, "description")) {
			descriptionNode = doc.createElement("description");
			descriptionNode
					.setTextContent("Puts the wrapper script '"
							+ scriptName
							+ "' in /usr/local/bin. For this operation to succeed the installer must be run with root privileges!");
			rootInstallPackNode.appendChild(descriptionNode);
		}

		if (!childExists(rootInstallPackNode, "file")) {
			fileNode = doc.createElement("file");
			attrs = fileNode.getAttributes();
			attrs.setNamedItem(createAttribute("override", "true"));
			attrs.setNamedItem(createAttribute("src", scriptName));
			attrs.setNamedItem(createAttribute("targetdir", "/usr/local/bin"));
			rootInstallPackNode.appendChild(fileNode);
		}

		if (!childExists(rootInstallPackNode, "parsable")) {
			parsableNode = doc.createElement("parsable");
			attrs = parsableNode.getAttributes();
			attrs.setNamedItem(createAttribute("type", "shell"));
			attrs.setNamedItem(createAttribute("targetfile", "/usr/local/bin/"
					+ scriptName));
			rootInstallPackNode.appendChild(parsableNode);
		}

		if (!childExists(rootInstallPackNode, "executable")) {
			executableNode = doc.createElement("executable");
			attrs = executableNode.getAttributes();
			attrs.setNamedItem(createAttribute("targetfile", "/usr/local/bin/"
					+ scriptName));
			attrs.setNamedItem(createAttribute("keep", "true"));
			attrs.setNamedItem(createAttribute("stage", "never"));
			rootInstallPackNode.appendChild(executableNode);
		}

	}

	void addWindowsWrapperScript(String scriptName, String appDescription)
			throws MojoExecutionException {
		Node requiredPackNode = getAppPackNode();

		Node descriptionNode = doc.createElement("description");
		descriptionNode.setTextContent(appDescription);
		requiredPackNode.appendChild(descriptionNode);

		Node fileNode = doc.createElement("file");
		NamedNodeMap attrs = fileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", scriptName));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		attrs.setNamedItem(createAttribute("os", "windows"));
		requiredPackNode.appendChild(fileNode);

		Node parsableNode = doc.createElement("parsable");
		attrs = parsableNode.getAttributes();
		attrs.setNamedItem(createAttribute("type", "shell"));
		attrs.setNamedItem(createAttribute("targetfile", "$INSTALL_PATH/"
				+ scriptName));
		attrs.setNamedItem(createAttribute("os", "windows"));
		requiredPackNode.appendChild(parsableNode);
	}

	void addStarter(String starterDir, String classpathPropertiesFile)
			throws MojoExecutionException {
		Node requiredPackNode = getAppPackNode();

		Node fileNode = doc.createElement("file");
		NamedNodeMap attrs = fileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", starterDir));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		requiredPackNode.appendChild(fileNode);

		Node parsableNode = doc.createElement("parsable");
		attrs = parsableNode.getAttributes();
		attrs.setNamedItem(createAttribute("type", "shell"));
		attrs.setNamedItem(createAttribute("targetfile", "$INSTALL_PATH/"
				+ starterDir + "/" + classpathPropertiesFile));
		requiredPackNode.appendChild(parsableNode);
	}

	/**
	 * Finds the aot pack by XML id and removes it.
	 * 
	 * This makes it possible that someone adds information for the aot pack in
	 * the descriptor but disables it in the POM.
	 * 
	 * The obvious need for this is that GCJ is sometimes incapable of compiling
	 * certain programs.
	 */
	void removeAotPack() {
		Node packNode = doc.getElementById(AOT_PACK_ID);
		if (packNode != null) {
			packNode.getParentNode().removeChild(packNode);
		}
	}

	/**
	 * Adds a pack entry for the aot compiled binary or adjusts an existing one
	 * which has been specifically marked.
	 * 
	 * @param aotRoot
	 * @param fixClassmapJar
	 * @param fixClassmapScript
	 */
	void addAotPack(String aotRoot, String fixClassmapJar,
			String fixClassmapScript) {
		// Try to lookup the pack.
		Node packNode = doc.getElementById(AOT_PACK_ID);
		NamedNodeMap attrs;

		// If it does not exist create a new one from scratch.
		if (packNode == null) {
			Node packsNode = doc.getElementsByTagName("packs").item(0);

			packNode = doc.createElement("pack");
			attrs = packNode.getAttributes();
			attrs.setNamedItem(createAttribute("name", "natively compiled"));
			attrs.setNamedItem(createAttribute("required", "no"));
			packsNode.appendChild(packNode);

			Node descriptionNode = doc.createElement("description");
			descriptionNode
					.setTextContent("Natively compiled version of the application.");
			packNode.appendChild(descriptionNode);
		}

		// Adds a node which causes copying the folder containing all the aot
		// stuff.
		Node aotFileNode = doc.createElement("file");
		attrs = aotFileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", aotRoot));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		packNode.appendChild(aotFileNode);

		// Adds a node which causes the copying of the fixclassmap program's jar
		// file.
		Node jarFileNode = doc.createElement("file");
		attrs = jarFileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", fixClassmapJar));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		packNode.appendChild(jarFileNode);

		// Adds a node which causes the copying the fixclassmap program's
		// wrapper script file.
		Node scriptFileNode = doc.createElement("file");
		attrs = scriptFileNode.getAttributes();
		attrs.setNamedItem(createAttribute("src", fixClassmapScript));
		attrs.setNamedItem(createAttribute("override", "true"));
		attrs.setNamedItem(createAttribute("targetdir", "$INSTALL_PATH"));
		packNode.appendChild(scriptFileNode);

		// Adds a node which causes the The fixclassmap program's wrapper script
		// file.
		Node parsableNode = doc.createElement("parsable");
		attrs = parsableNode.getAttributes();
		attrs.setNamedItem(createAttribute("type", "shell"));
		attrs.setNamedItem(createAttribute("targetfile", "$INSTALL_PATH/"
				+ fixClassmapScript));
		attrs.setNamedItem(createAttribute("os", "unix"));
		packNode.appendChild(parsableNode);

	}

	private Node createAttribute(String name, String value) {
		Attr attr = doc.createAttribute(name);
		attr.setValue(value);

		return attr;
	}

	private boolean childExists(Node parent, String childName) {
		NodeList list = parent.getChildNodes();
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			if (childName.equals(list.item(i).getLocalName())) {
				return true;
			}
		}
		return false;
	}

	private boolean attributeExists(NamedNodeMap attrs, String name) {
		return attrs.getNamedItem(name) != null;
	}

}
