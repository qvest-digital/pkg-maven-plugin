package de.tarent.maven.plugins.pkg.upload;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import de.tarent.maven.plugins.pkg.AbstractMvnPkgPluginTestCase;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.Upload;
import de.tarent.maven.plugins.pkg.WorkspaceSession;
import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class WagonUploaderTest extends AbstractMvnPkgPluginTestCase{
	
	File expectedPackageFile;
	WagonUploader wu;
	String expectedUrl;
	WorkspaceSession ws;
	Upload up;
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
		up = mockUploadEnvironment(UPLOADPOM);
		ws = new WorkspaceSession();
		PackageMap expectedPackageMap = new PackageMap(null, null, "dull", new HashSet<String>());
		ws.setPackageMap(expectedPackageMap);
		ws.setMojo(up);
		expectedUrl = "someurl";
		Helper h = new Helper();
		h.init(up, expectedPackageMap, new TargetConfiguration().fixate(), new ArrayList<TargetConfiguration>(),"ubuntu-lucid");
		ws.setHelper(h);
		expectedPackageFile = new File(ws.getMojo().getTempRoot().getParentFile(), ws.getHelper().getPackageFileName());
		wu = new WagonUploader(ws, expectedUrl);
		
		
	}
	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	@Test
	public void testConstructor() throws Exception{
		Assert.assertEquals(expectedUrl,getValueOfFieldInObject("url",wu));
		Assert.assertEquals(up.getLog(),getValueOfFieldInObject("l",wu));
		Assert.assertEquals(ws.getMojo().getProject(), getValueOfFieldInObject("project",wu));
		Assert.assertEquals(ws.getMojo().getPluginManager(), getValueOfFieldInObject("pluginManager",wu));
		Assert.assertEquals(ws.getMojo().getSession(), getValueOfFieldInObject("session",wu));
		Assert.assertEquals(expectedPackageFile, getValueOfFieldInObject("packageFile",wu));
	}
	
	@Test
	public void generateUploadElements() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{	
		Method m = WagonUploader.class.getDeclaredMethod("generateUploadElements",new Class[]{File.class, String.class});
		m.setAccessible(true);
		Element[] elementArray = (Element[]) m.invoke(wu,new Object[]{expectedPackageFile,expectedUrl});
		Element element0 = elementArray[0];
		Element element1 = elementArray[1];
		
		assertTrue(element0.toDom().toString().contains("<fromFile>"+expectedPackageFile+"</fromFile>"));
		assertTrue(element1.toDom().toString().contains("<url>"+expectedUrl+"</url>"));
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