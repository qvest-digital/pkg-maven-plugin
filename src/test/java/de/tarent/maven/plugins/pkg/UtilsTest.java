package de.tarent.maven.plugins.pkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.testingstubs.PkgProjectStub;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UtilsTest extends AbstractMvnPkgPluginTestCase{
	@Test
	public void testGetTargetConfigurationFromString(){
		
		List<TargetConfiguration> l = new ArrayList<TargetConfiguration>();
		TargetConfiguration t1 = new TargetConfiguration();
		TargetConfiguration t2 = new TargetConfiguration();
		TargetConfiguration t3 = new TargetConfiguration();
		TargetConfiguration t4 = new TargetConfiguration();
		l.add(t1);
		l.add(t2);
		l.add(t3);
		l.add(t4);
		
		t1.target = "unwantedConfig";
		t2.target = "unwantedConfig";
		t4.target = "unwantedConfig";	
		
		t3.target = "wantedConfig";
		t3.chosenDistro = "wantedDistro";
		
		Assert.assertEquals("wantedDistro",Utils.getTargetConfigurationFromString("wantedConfig", l).chosenDistro);
		
		
	}
	
	@Test
	public void testGetLicenseForProject() throws Exception{
		
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
	
	@Test
	public void testGetLicenseForProjectWIthoutLicenses() throws Exception{
		
		Packaging p = (Packaging)mockEnvironment("simplepom.xml", "pkg");
		p.project.setLicenses(null);
		try{
			Utils.getConsolidatedLicenseString(p.project);
		}catch(MojoExecutionException ex){
        	assertTrue(ex.toString().contains("Please provide at least one license in your POM."));           	
        }		
	}
	
	@Test
	public void testGetLicenseFromUlr() throws IOException{
		Assert.assertTrue(Utils.getTextFromUrl("http://www.gnu.org/licenses/lgpl.txt").
				contains("GNU LESSER GENERAL PUBLIC LICENSE"));
		
		
	}

}
