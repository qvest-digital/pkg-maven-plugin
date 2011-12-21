package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;


public class MvnPkgPluginUploadTest extends AbstractMvnPkgPluginTestCase{
	
	private static class TestStruct {
		private Upload upload;
		private PackageMap packageMap;
		private TargetConfiguration targetConfiguration;
		private String target; 
		private Helper helper;
	}
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
	}
	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	/*
	 * TODO: Create new, meaningful tests for the upload mechanism. 
	 * 
	 */
	
	@Test
	public void Upload() throws MojoExecutionException, IOException{		
		Assert.assertTrue(true);
	}
	
	private TestStruct mockUploadEnvironment() throws Exception {
		TestStruct ts = new TestStruct();
		Upload u = (Upload)mockEnvironment("uploadpom.xml", "upload");
		TargetConfiguration tc = Utils.getTargetConfigurationFromString("ubuntu_lucid_upload", u.targetConfigurations);
		tc.fixate();
		String t = tc.getTarget();
		PackageMap pm = new PackageMap(null, null, "ubuntu_lucid", null);
		
	    Helper ph = new Helper();
	    ph.init(u, pm, tc, null);
	    
		ts.upload = u;
		ts.targetConfiguration = tc;
		ts.target = t;
		ts.packageMap = pm;
		ts.helper = ph;
		
		return ts;
	}
}
