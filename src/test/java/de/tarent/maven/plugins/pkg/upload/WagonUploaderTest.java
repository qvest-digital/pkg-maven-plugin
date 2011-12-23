package de.tarent.maven.plugins.pkg.upload;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.AbstractMvnPkgPluginTestCase;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.Upload;
import de.tarent.maven.plugins.pkg.WorkspaceSession;
import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class WagonUploaderTest extends AbstractMvnPkgPluginTestCase{
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
	}
	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	@Test
	public void testConstructor() throws Exception{
		Upload up = mockUploadEnvironment(UPLOADPOM);
		WorkspaceSession ws = new WorkspaceSession();
		PackageMap expectedPackageMap = new PackageMap(null, null, "dull", new HashSet<String>());
		ws.setPackageMap(expectedPackageMap);
		ws.setMojo(up);
		String expectedUrl = "someurl";
		Helper h = new Helper();
		h.init(up, expectedPackageMap, new TargetConfiguration().fixate(), new ArrayList<TargetConfiguration>());
		ws.setHelper(h);
		File expectedPackageFile = new File(ws.getHelper().getTempRoot(), ws.getHelper().getPackageFileName());
		
		WagonUploader wu = new WagonUploader(ws, expectedUrl);
		
		

		Assert.assertEquals(expectedUrl,getValueOfFieldInObject("url",wu));
		Assert.assertEquals(up.getLog(),getValueOfFieldInObject("l",wu));
		Assert.assertEquals(ws.getMojo().getProject(), getValueOfFieldInObject("project",wu));
		Assert.assertEquals(ws.getMojo().getPluginManager(), getValueOfFieldInObject("pluginManager",wu));
		Assert.assertEquals(ws.getMojo().getSession(), getValueOfFieldInObject("session",wu));
		Assert.assertEquals(expectedPackageFile, getValueOfFieldInObject("packageFile",wu));
	}
	
	public Upload mockUploadEnvironment(String pomFilename) throws Exception{		
		 return (Upload)mockEnvironment(pomFilename,"upload",true);		
	}
	
	public Object getValueOfFieldInObject(String needle,Object obj) throws IllegalArgumentException, IllegalAccessException{
		
		Field[] allFields = WagonUploader.class.getDeclaredFields();
		
		for (int i = 0; i < allFields.length; i++) {
			if(allFields[i].getName()==needle){
				allFields[i].setAccessible(true);
				return allFields[i].get(obj); 
			}	
		}
		return null;
		
	}

}