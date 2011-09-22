package de.tarent.maven.plugins.pkg.map;

import org.apache.maven.plugin.MojoExecutionException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LoadMapsTest extends TestCase {

	/**
	 * Add the list of distros supported by the plugin here to ensure that all the
	 * configuration files are parsed during the test step.
	 */
	static final String[] distros = {
			"debian_etch",
			"debian_lenny",
			"maemo_bora",
			"maemo_chinook",
			"openmoko_2007.11",
			"ubuntu_gutsy",
			"ubuntu_hardy",
			"ubuntu_intrepid",
			"ubuntu_jaunty",
			"ubuntu_karmic",
			"ubuntu_lucid",
	};

	/**
	 * Loads each package map to ensure that it can be successfully parsed.
	 */
	public void testLoadMaps()
	{
		for (String d : distros)
		{
			try {
				new PackageMap(null, null, d, null);
			} catch (MojoExecutionException e) {
				Assert.fail(e.getLocalizedMessage());
			}
		}
	}
	
}
