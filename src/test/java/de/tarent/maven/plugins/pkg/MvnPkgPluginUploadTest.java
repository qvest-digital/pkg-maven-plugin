package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
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
	@Test
	public void generateUploadElements() throws MojoExecutionException, IOException{
		Upload u = new Upload();
		File f = File.createTempFile("mvnpkg", "");
		Element[] e = u.generateUploadElements(f, "file:///tmp/test", new UploadParameters());
		Assert.assertEquals(e.length, 2);
		UploadParameters param = new UploadParameters();
		param.setToDir("toDir");
		e = u.generateUploadElements(f, "file:///tmp/test", param);
		Assert.assertEquals(e.length, 3);
	}
	
	@Test
	public void getPackageFile() throws Exception{
		TestStruct ts = mockUploadEnvironment();
		Upload u = ts.upload;
		File f = u.getPackageFile(ts.targetConfiguration, ts.helper, "ubuntu_lucid_upload");
		Assert.assertNotNull(f);
		Assert.assertEquals("libdummyproject-java_1.0.0-0ubuntulucidupload-r0_all.deb",f.getName());		
	}
	
	@Test
	public void upload() throws Exception{
		/*
		 * TODO: Try to find a way to mock MavenSession in order for this
		 * test to run.
		Upload u = mockUploadEnvironment();
		File f = new File("/tmp/dummyproject_1.0.0-0ubuntulucidupload_all.deb");
		File f2 = new File("/tmp/2/dummyproject_1.0.0-0ubuntulucidupload_all.deb");
		File origin = new File(u.getBuildDir(),"dummyproject_1.0.0-0ubuntulucidupload_all.deb");
		origin.createNewFile();
		FileUtils.forceMkdir(f2.getParentFile());
		u.execute();
		Assert.assertTrue(f.exists());
		Assert.assertNotNull(f2.exists());
		f.delete();
		f2.delete();
		f2.getParentFile().delete();
		origin.delete();
		*/
		Assert.assertTrue(true);
		
				
	}
	
	private TestStruct mockUploadEnvironment() throws Exception {
		TestStruct ts = new TestStruct();
		Upload u = (Upload)mockEnvironment("uploadpom.xml", "upload");
		TargetConfiguration tc = Utils.getTargetConfigurationFromString("ubuntu_lucid_upload", u.targetConfigurations);
		// TODO: Self initialization.
		tc.merge(new TargetConfiguration(tc.getTarget()));
		String t = tc.getTarget();
		PackageMap pm = new PackageMap(null, null, "ubuntu_lucid", null);
		
	    Helper ph = Utils.getPackagingHelperForPackaging(pm.getPackaging());
	    ph.init(u, pm, tc, null);
	    
		ts.upload = u;
		ts.targetConfiguration = tc;
		ts.target = t;
		ts.packageMap = pm;
		ts.helper = ph;
		
		return ts;
	}
}
