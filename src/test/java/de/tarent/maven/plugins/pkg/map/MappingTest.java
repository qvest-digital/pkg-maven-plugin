package de.tarent.maven.plugins.pkg.map;


import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.After;
import org.junit.Before;

import de.tarent.maven.plugins.pkg.map.PackageMap;

public class MappingTest extends TestCase {
	
	private PackageMap pm;

	@Before
	public void setUp() throws Exception {
		pm = new PackageMap(null, null, "ubuntu_karmic", null);
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
