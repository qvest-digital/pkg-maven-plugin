package de.tarent.maven.plugins.pkg;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuxFileTest {
	AuxFile a;
	@Before
	public void setUP(){
		a = new AuxFile();
	}
	
	@Test
	public void testExecuteRights(){
		a.setUserExecute(true);
		a.setGroupExecute(true);
		a.setOthersExecute(true);
		Assert.assertEquals(111,a.getOctalPermission());
		Assert.assertTrue(a.isUserExecute());
		Assert.assertTrue(a.isGroupExecute());
		Assert.assertTrue(a.isOthersExecute());
	}
	
	@Test
	public void testWriteRights(){
		a.setUserWrite(true);
		a.setGroupWrite(true);
		a.setOthersWrite(true);
		Assert.assertEquals(222,a.getOctalPermission());
		Assert.assertTrue(a.isUserWrite());
		Assert.assertTrue(a.isGroupWrite());
		Assert.assertTrue(a.isOthersWrite());
	}
	
	@Test
	public void testReadRights(){
		a.setUserRead(true);
		a.setGroupRead(true);
		a.setOthersRead(true);
		Assert.assertEquals(444,a.getOctalPermission());
		Assert.assertTrue(a.isUserRead());
		Assert.assertTrue(a.isGroupRead());
		Assert.assertTrue(a.isOthersRead());
	}
	
	@Test
	public void testAllRights(){
		a.setUserRead(true);
		a.setGroupRead(true);
		a.setOthersRead(true);
		a.setUserWrite(true);
		a.setGroupWrite(true);
		a.setOthersWrite(true);
		a.setUserExecute(true);
		a.setGroupExecute(true);
		a.setOthersExecute(true);
		Assert.assertEquals(777,a.getOctalPermission());
		Assert.assertTrue(a.isUserRead());
		Assert.assertTrue(a.isGroupRead());
		Assert.assertTrue(a.isOthersRead());
		Assert.assertTrue(a.isUserWrite());
		Assert.assertTrue(a.isGroupWrite());
		Assert.assertTrue(a.isOthersWrite());
		Assert.assertTrue(a.isUserExecute());
		Assert.assertTrue(a.isGroupExecute());
		Assert.assertTrue(a.isOthersExecute());
	}
	
	@Test
	public void testSetGroup(){
		a.setGroup("mygroup");
		Assert.assertEquals("mygroup",a.getGroup());
	}
	
	@Test
	public void testSetOwner(){
		a.setOwner("me");
		Assert.assertEquals("me",a.getOwner());
	}
	
	@Test
	public void testGetGroupReturnsRootByDefault(){
		Assert.assertEquals("root",a.getGroup());
	}
	
	@Test
	public void testGetOwnerReturnsRootByDefault(){
		Assert.assertEquals("root",a.getOwner());
	}

}
