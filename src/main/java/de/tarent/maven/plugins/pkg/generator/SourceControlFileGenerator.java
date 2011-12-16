/**
 * 
 */
package de.tarent.maven.plugins.pkg.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Fabian K&ouml;ster (f.koester@tarent.de) tarent GmbH Bonn
 *
 */
public class SourceControlFileGenerator extends ControlFileGenerator {

	/**
	 * @see de.tarent.maven.plugins.pkg.generator.ControlFileGenerator#generate(java.io.File)
	 */
	public void generate(File f) throws IOException {
		PrintWriter w = new PrintWriter(new FileOutputStream(f));
	    
		writeEntry(w, "Source", getSource());
		w.println();
	    writeEntry(w, "Package", getPackageName());
	    writeEntry(w, "Version", getVersion());
	    writeEntry(w, "Section", getSection());
	    writeEntry(w, "Depends", getDependencies());
	    writeEntry(w, "Priority", "optional");
	    writeEntry(w, "Architecture", getArchitecture());
	    writeEntry(w, "OE", getOE());
	    writeEntry(w, "Homepage", getHomepage());
	    writeEntry(w, "Installed-Size", getInstalledSize());
	    writeEntry(w, "Maintainer", getMaintainer());
	    writeEntry(w, "Description", getShortDescription());
	    
	    if (getDescription() != null) {
	      w.println(" " + getDescription());
	    }
	    w.close();
	}
	
	
}
