package de.tarent.maven.plugins.pkg.upload;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import de.tarent.maven.plugins.pkg.AbstractMvnPkgPluginTestCase;
import de.tarent.maven.plugins.pkg.Upload;
import de.tarent.maven.plugins.pkg.WorkspaceSession;
import de.tarent.maven.plugins.pkg.map.PackageMap;


public class APTUploaderTest extends AbstractMvnPkgPluginTestCase{
	
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
		String expectedRepo = "localrepo";
		ws.setPackageMap(expectedPackageMap);
		ws.setMojo(up);
		APTUploader au = new APTUploader(ws, expectedRepo);
		
		Assert.assertEquals(expectedPackageMap,getValueOfFieldInObject("packageMap",au));
		Assert.assertEquals(expectedPackageMap.getPackaging(),((PackageMap)getValueOfFieldInObject("packagingType",au)));
		Assert.assertEquals(expectedRepo,getValueOfFieldInObject("repo",au));
		Assert.assertEquals(up.getLog(),getValueOfFieldInObject("l",au));
		Assert.assertEquals(ws.getMojo().getBuildDir(),getValueOfFieldInObject("base",au));
	}
	
	public Upload mockUploadEnvironment(String pomFilename) throws Exception{		
		 return (Upload)mockEnvironment(pomFilename,"upload",true);		
	}
	
	public Object getValueOfFieldInObject(String needle,Object obj) throws IllegalArgumentException, IllegalAccessException{
		
		Field[] allFields = APTUploader.class.getDeclaredFields();
		
		for (int i = 0; i < allFields.length; i++) {
			if(allFields[i].getName()==needle){
				allFields[i].setAccessible(true);
				return allFields[i].get(obj); 
			}	
		}
		return null;
		
	}

}
