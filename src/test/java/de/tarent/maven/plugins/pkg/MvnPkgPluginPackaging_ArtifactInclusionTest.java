package de.tarent.maven.plugins.pkg;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MvnPkgPluginPackaging_ArtifactInclusionTest extends
		AbstractMvnPkgPluginTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {

		super.tearDown();
	}

	public Packaging mockPackagingEnvironment(String pomFilename)
			throws Exception {
		return (Packaging) mockEnvironment(pomFilename, "pkg", true);
	}

	public Packaging mockPackagingEnvironment(String pomFilename, String target)
			throws Exception {
		return (Packaging) mockEnvironment(pomFilename, "pkg", true, target);

	}

	@Test
	public void testDefaultArtifactInclusionStrategy()
			throws Exception, MojoExecutionException {
		packagingPlugin = mockPackagingEnvironment(INCLUSIONSTRATEGIESPOM,
				"default_inclusionstrategy");
		packagingPlugin.execute();
		assertTrue(numberOfDEBsIs(1));
		assertTrue(debContainsMainArtifact());
		
		// TODO: For this to work the artifact resolution needs to work for mocked POMs
		// first. It does not do that however and it seems difficult to achieve.
		//assertTrue(debContainsFile("usr/share/java/DummyProject/commons-io-2.0.1.jar"));
	}

	@Test
	public void testNoneArtifactInclusionStrategy()
			throws Exception, MojoExecutionException {
		packagingPlugin = mockPackagingEnvironment(INCLUSIONSTRATEGIESPOM,
				"none_inclusionstrategy");
		packagingPlugin.execute();
		assertTrue(numberOfDEBsIs(1));
		assertFalse(debContainsMainArtifact());
		assertFalse(debContainsFile("usr/share/java/DummyProject/commons-io-2.0.1.jar"));
	}

	@Test
	public void testProjectArtifactInclusionStrategy()
			throws Exception, MojoExecutionException {
		packagingPlugin = mockPackagingEnvironment(INCLUSIONSTRATEGIESPOM,
				"project_inclusionstrategy");
		packagingPlugin.execute();
		assertTrue(numberOfDEBsIs(1));
		assertTrue(debContainsMainArtifact());
		assertFalse(debContainsFile("usr/share/java/DummyProject/commons-io-2.0.1.jar"));
	}

	@Test
	public void testDependenciesArtifactInclusionStrategy()
			throws Exception, MojoExecutionException {
		packagingPlugin = mockPackagingEnvironment(INCLUSIONSTRATEGIESPOM,
				"dependencies_inclusionstrategy");
		packagingPlugin.execute();
		assertTrue(numberOfDEBsIs(1));
		assertFalse(debContainsMainArtifact());
		
		// TODO: For this to work the artifact resolution needs to work for mocked POMs
		// first. It does not do that however and it seems difficult to achieve.
		//assertTrue(debContainsFile("usr/share/java/DummyProject/commons-io-2.0.1.jar"));
	}
	
}
