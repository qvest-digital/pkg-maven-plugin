/**
 * 
 */
package de.tarent.maven.plugins.pkg.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabian K&ouml;ster (f.koester@tarent.de) tarent GmbH Bonn
 *
 */
public class ChangelogFileGenerator {

	private String packageName;

	private String version;

	private String maintainer;
	
	private String date;	
	
	private String repositoryName;
	
	private List<String> changes;

	public String getMaintainer()
	{
		return maintainer;
	}

	public void setMaintainer(String maintainer)
	{
		this.maintainer = maintainer;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	
	}
	
	public void setRepositoryName(String repoName) {
		this.repositoryName = repoName;
	}
	
	public List<String> getChanges() {
		return changes;
	}
	
	public void setChanges(List<String> changes) {
		this.changes = changes;
	}

	public void generate(File f) throws IOException
	{
		PrintWriter w = new PrintWriter(new FileOutputStream(f));

		w.println(getPackageName() + " (" + getVersion() + ") " + getRepositoryName() + "; urgency=low");
		w.println();
		
		Iterator<String> it = getChanges().iterator();
		
		while(it.hasNext())
			w.println("  * "+it.next());
		
		w.println();
		w.println(" -- "+getMaintainer() + "  " + getDate());

		w.close();
	}
}
