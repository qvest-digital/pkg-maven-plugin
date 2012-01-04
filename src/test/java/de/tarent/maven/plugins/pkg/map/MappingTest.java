package de.tarent.maven.plugins.pkg.map;

import java.net.URL;
import java.util.HashSet;

import junit.framework.TestCase;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import de.tarent.maven.plugins.pkg.exception.XMLParserException;

public class MappingTest extends TestCase {

	public void testEntryWithVersionRange() {
		URL url = MappingTest.class.getResource("pm-MappingTest.xml");
		Parser p;
		try {
			p = new Parser(url, null);
		} catch (XMLParserException e) {
			throw new IllegalStateException(
					"Exception occured during test setup.", e);
		}

		Mapping m = p.getMapping("mappingtest");
		ArtifactVersion v = null;
		Entry e = null;
		String expected = null;

		// Should give us libcommons-collections
		v = new DefaultArtifactVersion("2.0");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections-java";
		assertEquals(expected, e.dependencyLine);

		// Should give us libcommons-collections
		v = new DefaultArtifactVersion("2.9-beta18");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections-java";
		assertEquals(expected, e.dependencyLine);

		// Should give us libcommons-collections3
		v = new DefaultArtifactVersion("3.0");
		e = m.getEntry("commons-collections", "commons-collections", v);
		expected = "libcommons-collections3-java";
		assertEquals(expected, e.dependencyLine);

		// Should us nothing
		v = new DefaultArtifactVersion("2.0");
		e = m.getEntry("junit", "junit", v);
		expected = null;
		assertEquals(null, e);

		// Should give us junit (3.x)
		v = new DefaultArtifactVersion("3");
		e = m.getEntry("junit", "junit", v);
		expected = "junit";
		assertEquals(expected, e.dependencyLine);

		// Should give us junit4
		v = new DefaultArtifactVersion("4.0");
		e = m.getEntry("junit", "junit", v);
		expected = "junit4";
		assertEquals(expected, e.dependencyLine);

		// Should give us (fictional) junit5
		v = new DefaultArtifactVersion("5.0");
		e = m.getEntry("junit", "junit", v);
		expected = "junit5";
		assertEquals(expected, e.dependencyLine);

		// Should give us nothing
		v = new DefaultArtifactVersion("6.0");
		e = m.getEntry("junit", "junit", v);
		expected = null;
		assertEquals(null, e);
	}

	/**
	 * Tests whether the simple properties of a mapping are
	 * <ul>
	 * <li>properly inherited when not overridden,</li>
	 * <li>properly overridden when overridden.</li>
	 * </ul>
	 */
	public void testSimpleInheritance() {
		Mapping parent = new Mapping("parent");
		parent.parent = null;
		parent.label = "Parent test distro";
		parent.packaging = "deb";
		parent.defaultBinPath = "/usr/bin";
		parent.defaultJarPath = "/usr/share/java";
		parent.defaultJNIPath = "/usr/lib/jni:/usr/lib";
		parent.defaultDependencyLine = "java2-runtime";
		parent.debianNaming = true;
		parent.repoName = "parent-repo";

		Mapping tchild = new Mapping("child");
		tchild.parent = "parent";

		Mapping child = new Mapping(tchild, parent);

		// Everything inherited from parent
		assertEquals("parent", child.parent);
		assertEquals("Parent test distro", child.label);
		assertEquals("deb", child.packaging);
		assertEquals("/usr/bin", child.defaultBinPath);
		assertEquals("/usr/share/java", child.defaultJarPath);
		assertEquals("/usr/lib/jni:/usr/lib", child.defaultJNIPath);
		assertEquals("java2-runtime", child.defaultDependencyLine);
		assertEquals(Boolean.TRUE, child.debianNaming);
		assertEquals("parent-repo", child.repoName);

		tchild = new Mapping("child2");
		tchild.parent = "parent";
		tchild.label = "Child2 test distro";
		parent.packaging = "ipk";
		parent.defaultBinPath = "/opt/usr/bin";
		parent.defaultJarPath = "/opt/usr/share/java";
		parent.defaultJNIPath = "/opt/usr/lib/jni";
		parent.defaultDependencyLine = "openjdk6";
		parent.debianNaming = Boolean.FALSE;
		parent.repoName = "child2-repo";

		child = new Mapping(tchild, parent);

		// Everything overridden in child
		assertEquals(parent.distro, child.parent);
		assertEquals("Child2 test distro", child.label);
		assertEquals("ipk", child.packaging);
		assertEquals("/opt/usr/bin", child.defaultBinPath);
		assertEquals("/opt/usr/share/java", child.defaultJarPath);
		assertEquals("/opt/usr/lib/jni", child.defaultJNIPath);
		assertEquals("openjdk6", child.defaultDependencyLine);
		assertEquals(Boolean.FALSE, child.debianNaming);
		assertEquals("child2-repo", child.repoName);
	}

	/**
	 * Tests whether:
	 * <ul>
	 * <li>entries from parent are properly inherited to children</li>
	 * <li>entries from parent, that are replaced in children are really replaced</li>
	 * <li>entries added to children are not in parent</li>
	 * </ul>
	 * 
	 * <p>
	 * This is all done without version ranges.
	 * </p>
	 */
	public void testInheritanceNoVersionRange() {
		Mapping parent = new Mapping("parent");
		parent.parent = null;
		parent.putEntry(Entry.createBundleEntry("groupid:inherit", null));
		parent.putEntry(Entry.createIgnoreEntry("groupid:replace", null));

		Mapping tchild = new Mapping("child");

		tchild.putEntry(new Entry("groupid:replace", null, "libreplace2-java",
				createHashSet(new String[] { "replace2.jar" }), false));

		tchild.putEntry(new Entry("groupid:new", null, "libnew-java",
				createHashSet(new String[] { "new.jar" }), false));

		Mapping child = new Mapping(tchild, parent);
		Entry e;
		HashSet<String> jarFileNames;

		e = parent.getEntry("groupid", "inherit", null);
		assertEquals("parent-inherit artifactSpec", "groupid:inherit",
				e.artifactSpec);
		assertEquals("parent-inherit bundleEntry", true, e.bundleEntry);
		assertEquals("parent-inherit ignoreEntry", false, e.ignoreEntry);

		e = parent.getEntry("groupid", "replace", null);
		assertEquals("parent-replace artifactSpec", "groupid:replace",
				e.artifactSpec);
		assertEquals("parent-replace bundleEntry", false, e.bundleEntry);
		assertEquals("parent-replace ignoreEntry", true, e.ignoreEntry);

		// This entry should not exist in parent
		e = parent.getEntry("groupid", "new", null);
		assertEquals("parent-new", null, e);

		e = child.getEntry("groupid", "inherit", null);
		assertEquals("child-inherit artifactSpec", "groupid:inherit",
				e.artifactSpec);
		assertEquals("child-inherit bundleEntry", true, e.bundleEntry);
		assertEquals("child-inherit ignoreEntry", false, e.ignoreEntry);

		e = child.getEntry("groupid", "replace", null);
		assertEquals("child-replace artifactSpec", "groupid:replace",
				e.artifactSpec);
		assertEquals("child-replace dependencyLine", "libreplace2-java",
				e.dependencyLine);
		jarFileNames = new HashSet<String>();
		jarFileNames.add("replace2.jar");
		assertEquals("child-replace jarFileNames", jarFileNames, e.jarFileNames);

		e = child.getEntry("groupid", "new", null);
		assertEquals("child-new artifactSpec", "groupid:new", e.artifactSpec);
		assertEquals("child-new dependencyLine", "libnew-java",
				e.dependencyLine);
		jarFileNames = new HashSet<String>();
		jarFileNames.add("new.jar");
		assertEquals("child-new jarFileNames", jarFileNames, e.jarFileNames);

	}
	
	/**
	 * Tests whether:
	 * <ul>
	 * <li>entries from parent are properly inherited to children</li>
	 * <li>entries from parent, that are replaced in children are really replaced</li>
	 * <li>entries added to children are not in parent</li>
	 * </ul>
	 * 
	 * <p>
	 * This is all done without version ranges.
	 * </p>
	 */
	public void testInheritanceWithVersionRange() {
		Mapping parent = new Mapping("parent");
		parent.parent = null;
		try {
			parent.putEntry(new Entry("groupid:replace", VersionRange.createFromVersionSpec("[1.0,2.0)"), "libreplace1-java", null, false));
			parent.putEntry(new Entry("groupid:replace", VersionRange.createFromVersionSpec("[2.0,3.0)"), "libreplace2-java", null, false));
			parent.putEntry(new Entry("groupid:replace", VersionRange.createFromVersionSpec("[3.0,4.0)"), "libreplace3-java", null, false));
		} catch (InvalidVersionSpecificationException e) {
			throw new IllegalStateException("invalid versions given", e);
		}

		Mapping tchild = new Mapping("child");
		try {
			// Version 1 should be inherited.
			
			// Version 2 with new dependency line.
			tchild.putEntry(new Entry("groupid:replace", VersionRange.createFromVersionSpec("[2.0,3.0)"), "libreplace2-java libreplace2-additional-java", null, false));
			
			// Version 3 with ignore property.
			tchild.putEntry(Entry.createIgnoreEntry("groupid:replace", VersionRange.createFromVersionSpec("[3.0,4.0)")));
			
			// Version 4 is new
			tchild.putEntry(new Entry("groupid:replace", VersionRange.createFromVersionSpec("[4.0,5.0)"), "libreplace4-java", null, false));
			
		} catch (InvalidVersionSpecificationException e) {
			throw new IllegalStateException("invalid versions given", e);
		}

		Mapping child = new Mapping(tchild, parent);
		Entry e;

		// Should not exist
		e = parent.getEntry("groupid", "replace", new DefaultArtifactVersion("0.5"));
		assertEquals("parent-replace-0.5", null, e);

		// Should exist
		e = parent.getEntry("groupid", "replace", new DefaultArtifactVersion("1.1"));
		assertEquals("parent-replace-1.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("parent-replace-1.x dependencyLine", "libreplace1-java", e.dependencyLine);
		
		// Should exist
		e = parent.getEntry("groupid", "replace", new DefaultArtifactVersion("2.5"));
		assertEquals("parent-replace-2.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("parent-replace-2.x dependencyLine", "libreplace2-java", e.dependencyLine);

		// Should exist
		e = parent.getEntry("groupid", "replace", new DefaultArtifactVersion("3.0"));
		assertEquals("parent-replace-3.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("parent-replace-3.x dependencyLine", "libreplace3-java", e.dependencyLine);

		// Should not exist
		e = parent.getEntry("groupid", "replace", new DefaultArtifactVersion("4.7"));
		assertEquals("parent-replace-4.x", null, e);

		// Now on to the child
		
		// Should not exist
		e = child.getEntry("groupid", "replace", new DefaultArtifactVersion("0.5"));
		assertEquals("child-replace-0.5", null, e);

		// Should exist (and stay the same as in parent)
		e = child.getEntry("groupid", "replace", new DefaultArtifactVersion("1.1"));
		assertEquals("child-replace-1.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("child-replace-1.x dependencyLine", "libreplace1-java", e.dependencyLine);
		
		// Should exist (and should have different dependency line)
		e = child.getEntry("groupid", "replace", new DefaultArtifactVersion("2.5"));
		assertEquals("child-replace-2.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("child-replace-2.x dependencyLine", "libreplace2-java libreplace2-additional-java", e.dependencyLine);

		// Should exist
		e = child.getEntry("groupid", "replace", new DefaultArtifactVersion("3.0"));
		assertEquals("child-replace-3.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("child-replace-3.x ignoreEntry", true, e.ignoreEntry);

		// Should now exist
		e = child.getEntry("groupid", "replace", new DefaultArtifactVersion("4.7"));
		assertEquals("child-replace-4.x artifactSpec", "groupid:replace", e.artifactSpec);
		assertEquals("child-replace-4.x dependencyLine", "libreplace4-java", e.dependencyLine);


	}
	
	private HashSet<String> createHashSet(String[] entries) {
		HashSet<String> hs = new HashSet<String>();
		for (String e : entries)
			hs.add(e);

		return hs;
	}
}
