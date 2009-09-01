package de.tarent.maven.plugins.pkg.map;


import java.lang.reflect.Field;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.After;
import org.junit.Before;

public class MappingTest extends TestCase {
	
	private PackageMap pm;

	@Before
	public void setUp() throws Exception {
		URL url = MappingTest.class.getResource("pm-MappingTest.xml");
		pm = new PackageMap(url, null, "mappingtest", null);
	}

	@After
	public void tearDown() throws Exception {
		pm = null;
	}

	public void testEntryWithVersionRange()
	{
		Mapping m = (Mapping) getFieldValue(pm, "mapping");
		ArtifactVersion v = null;
		Entry e = null;
		String expected = null;

		// Should give us libcommons-collections
		v = new DefaultArtifactVersion("2.0");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections-java";
		assertEquals(expected, e.packageName);
		
		// Should give us libcommons-collections
		v = new DefaultArtifactVersion("2.9-beta18");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections-java";
		assertEquals(expected, e.packageName);
		
		// Should give us libcommons-collections3
		v = new DefaultArtifactVersion("3.0");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections3-java";
		assertEquals(expected, e.packageName);
		
		// Should us nothing
		v = new DefaultArtifactVersion("2.0");
		e = m.getEntry("junit", "junit", v);
		expected = null;
		assertEquals(null, e);
		
		// Should give us junit (3.x)
		v = new DefaultArtifactVersion("3");
		e = m.getEntry("junit", "junit", v);
		expected = "junit";
		assertEquals(expected, e.packageName);
		
		// Should give us junit4
		v = new DefaultArtifactVersion("4.0");
		e = m.getEntry("junit", "junit", v);
		expected = "junit4";
		assertEquals(expected, e.packageName);

		// Should give us (fictional) junit5
		v = new DefaultArtifactVersion("5.0");
		e = m.getEntry("junit", "junit", v);
		expected = "junit5";
		assertEquals(expected, e.packageName);

		// Should us nothing
		v = new DefaultArtifactVersion("6.0");
		e = m.getEntry("junit", "junit", v);
		expected = null;
		assertEquals(null, e);
	}
	
	Object getFieldValue(Object o, String fieldName)
	{
		try {
			Field f = o.getClass().getDeclaredField(fieldName);
			
			f.setAccessible(true);
			
			return f.get(o);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
}
