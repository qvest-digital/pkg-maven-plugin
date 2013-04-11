package de.tarent.maven.plugins.pkg.merger;

import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class MergerTest extends TestCase {
	@Test
	public void ObjectMergerPropertiesTest(){
		
		Properties parent = new Properties();
		parent.put("testPropertyKey", "oldPropertyValue");
		parent.put("testPropertyKey2", "InheritedValue");
		Properties child = null;
		Object def = new Object();
		
		PropertiesMerger om = new PropertiesMerger();
		child = om.merge(child, parent, def);
		child.setProperty("testPropertyKey","newPropertyValue");		
	
		// The objets for testPropertyKey in parent and child should not be the same
		assertNotSame(child.get("testPropertyKey"), parent.get("testPropertyKey"));
		// and the value of the object in the parent should be oldPropertyValue
		assertEquals("oldPropertyValue",parent.get("testPropertyKey"));
		// Child should contain both 
		// <testPropertyKey, newPorpertyValue>
		// <testPropertyKey2, InheritedValue>
		assertTrue(child.getProperty("testPropertyKey").equals("newPropertyValue"));
		assertTrue(child.getProperty("testPropertyKey2").equals("InheritedValue"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void ObjectMergerCollectionsTest() throws InstantiationException, IllegalAccessException{
		
		Object parent = new ArrayList<String>();
		String parentString = "testValueParent";
		((ArrayList<String>)parent).add(parentString);
		Object child = null;
		Object def = new ArrayList<String>();
		
		CollectionMerger cm = new CollectionMerger();
		child = cm.merge(child, parent, def);
		String childString = "testValueChild";
		((ArrayList<String>)child).add(childString);
	
		// Parent should contain parentString
		assertTrue(((ArrayList<String>)parent).contains(parentString));	
		// Parent should not contain childString
		assertFalse(((ArrayList<String>)parent).contains(childString));
		
		// Child should contain parentString
		assertTrue(((ArrayList<String>)child).contains(childString));	
		// Child should contain childString
		assertTrue(((ArrayList<String>)child).contains(childString));
	}
}
