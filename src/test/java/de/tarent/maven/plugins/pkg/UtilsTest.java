package de.tarent.maven.plugins.pkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilsTest extends AbstractMvnPkgPluginTestCase{
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
	}
	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	@Test
	public void getTargetConfigurationFromString(){
		
		List<TargetConfiguration> l = new ArrayList<TargetConfiguration>();
		TargetConfiguration t1 = new TargetConfiguration();
		TargetConfiguration t2 = new TargetConfiguration();
		TargetConfiguration t3 = new TargetConfiguration();
		TargetConfiguration t4 = new TargetConfiguration();
		l.add(t1);
		l.add(t2);
		l.add(t3);
		l.add(t4);
		
		t1.setTarget("unwantedConfig");
		t2.setTarget("unwantedConfig");
		t4.setTarget("unwantedConfig");	
		
		t3.setTarget("wantedConfig");
		t3.setChosenDistro("wantedDistro");
		
		Assert.assertEquals(t3, Utils.getTargetConfigurationFromString("wantedConfig", l));
		
		
	}
	
	@Test	
	@SuppressWarnings("unchecked")
	public void getLicenseForProject() throws Exception{
		
		Packaging p = (Packaging)mockEnvironment("simplepom.xml", "pkg");
		List<License>licenses = createLicenseList("License 1", "License 2");
		
		p.project.setLicenses(licenses);
		String result = Utils.getConsolidatedLicenseString(p.project);
		Assert.assertEquals("License 1, License 2",result);
		
		List<License> l = p.project.getLicenses();
		l.remove(0);
		p.project.setLicenses(l);
		result = Utils.getConsolidatedLicenseString(p.project);
		Assert.assertEquals("License 2",result);
		
	}
	
	@Test(expected=MojoExecutionException.class)
	public void getLicenseForProjectWithoutLicenses() throws Exception{
		
		Packaging p = (Packaging)mockEnvironment("simplepom.xml", "pkg");
		p.project.setLicenses(null);
		Utils.getConsolidatedLicenseString(p.project);
	}
	
	@Test
	public void getLicenseFromUrl() throws IOException{
		Assert.assertTrue(Utils.getTextFromUrl("http://www.gnu.org/licenses/lgpl.txt").
				contains("GNU LESSER GENERAL PUBLIC LICENSE"));
	}
	
	/**
	 * Checks the core functionality of the {@link Utils#getMergedConfiguration()}
	 * method.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getMergedConfiguration() throws Exception {
		// Sets up 2 configuration where one inherits from the other.
		// When the method is called we expect a new instance which has
		// properties of both.
		
		// Just some random values that can be accessed by their
		// reference later (IOW the value is not important and has
		// no meaning).
		String set_in_t1 = "set_in_t1";
		String overridden_in_t2 = "overridden_in_t2";
		
		// Default configuration must exist for now to please
		// the method signature.
		TargetConfiguration defaultConfig = new TargetConfiguration();
		defaultConfig.setDistro("foo");
		
		TargetConfiguration t1 = new TargetConfiguration("t1");
		t1.setDistro("foo");
		t1.setPrefix(set_in_t1);
		t1.setMainClass(set_in_t1);
		
		TargetConfiguration t2 = new TargetConfiguration("t2");
		t2.parent = "t1";
		t2.setDistro("foo");
		t2.setMainClass(overridden_in_t2);
		
		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
		tcs.add(t1);
		tcs.add(t2);
		
		TargetConfiguration result =
				Utils.getMergedConfiguration("t2", "foo", true, tcs, defaultConfig);
		
		Assert.assertEquals("t2", result.getTarget());
		Assert.assertEquals("t1", result.parent);
		Assert.assertEquals("foo", result.getChosenDistro());
		Assert.assertEquals(set_in_t1, result.getPrefix());
		Assert.assertEquals(overridden_in_t2, result.getMainClass());
	}

}
