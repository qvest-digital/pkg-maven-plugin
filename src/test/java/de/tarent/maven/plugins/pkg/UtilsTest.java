package de.tarent.maven.plugins.pkg;

import java.io.IOException;
import java.util.ArrayList;
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
		
		Assert.assertEquals("wantedDistro",Utils.getTargetConfigurationFromString("wantedConfig", l).getChosenDistro());
		
		
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
	public void getLicenseForProjectWIthoutLicenses() throws Exception{
		
		Packaging p = (Packaging)mockEnvironment("simplepom.xml", "pkg");
		p.project.setLicenses(null);
		Utils.getConsolidatedLicenseString(p.project);
	}
	
	@Test
	public void getLicenseFromUlr() throws IOException{
		Assert.assertTrue(Utils.getTextFromUrl("http://www.gnu.org/licenses/lgpl.txt").
				contains("GNU LESSER GENERAL PUBLIC LICENSE"));
		
		
	}

}
