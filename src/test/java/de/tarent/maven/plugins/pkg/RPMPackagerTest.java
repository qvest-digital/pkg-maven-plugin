package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.Packaging;
import de.tarent.maven.plugins.pkg.TargetConfiguration;

public class RPMPackagerTest {
	Packaging p;
	Packaging.RPMHelper ph;
	TargetConfiguration dc;
	
	@Before
	public void setUp(){
		//TODO: Mock these property in order to write tests
		p = new Packaging();
		p.defaultDistro = "centos_5_6";
		p.defaultTarget = "noarch";
		p.project = mockProject();
		dc = mockTargetConfiguration();
		p.defaults = mockTargetConfiguration();
		ph = p.new RPMHelper();
		p.targetConfigurations = new ArrayList<TargetConfiguration>();
		p.targetConfigurations.add(dc);
		
	}


	@Test
	public void testCheckEnvironment() throws MojoExecutionException, MojoFailureException{
		Assert.assertTrue(true);
	}
	
	@Test
	public void testExecute() throws MojoExecutionException, MojoFailureException{
		Assert.assertTrue(true);
	}
	
	@After
	public void tearDown(){
		
	}

	private TargetConfiguration mockTargetConfiguration() {
		TargetConfiguration dc = new TargetConfiguration();
		dc.setAdvancedStarter(false);
		dc.setArchitecture("all");
		dc.setDistro("centos_5_6");
		dc.target = "noarch";
		dc.setLicense("GPL");
		dc.setMaintainer("Maintainer name");
		dc.setRelease("3.0.3");
		dc.setSource("No source");
		return dc;
	}
	
	private MavenProject mockProject() {
		MavenProject project = new MavenProject();
		project.setArtifactId("testproject");
		project.setBasedir(new File("/tmpBasedir"));
		project.setDescription("Dummy project description for testproject");
		project.setDevelopers(new ArrayList<String>());
		project.setGroupId("testgroupid");
		project.setName("testprojectname");
		project.setUrl("http://dummy.org");
		project.setVersion("3.0.1");
		return project;
	}
}
