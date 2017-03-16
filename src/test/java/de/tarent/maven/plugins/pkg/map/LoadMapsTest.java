package de.tarent.maven.plugins.pkg.map;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class LoadMapsTest extends TestCase {

	/**
	 * Add the list of distros supported by the plugin here to ensure that all
	 * the configuration files are parsed during the test step.
	 */
	private static final String[] distros = { "debian_etch", "debian_lenny", "debian_wheezy",
			"maemo_bora", "maemo_chinook", "openmoko_2007.11", "ubuntu_gutsy",
			"ubuntu_hardy", "ubuntu_intrepid", "ubuntu_jaunty",
			"ubuntu_karmic", "ubuntu_lucid", "ubuntu_precise", "ubuntu_trusty", "centos_5_6",
			"centos_5_7", "centos_6", "centos_6_1", "centos_6_2", };

	/**
	 * Loads each package map to ensure that it can be successfully parsed.
	 */
	@Test
	public void testLoadMaps() {
		for (String d : distros) {
			try {
				new PackageMap(null, null, d, null);
			} catch (MojoExecutionException e) {
				Assert.fail(e.getLocalizedMessage());
			}
		}
	}

}
