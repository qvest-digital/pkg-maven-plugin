package de.tarent.maven.plugins.pkg.upload;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;

import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.AbstractMvnPkgPluginTestCase;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.Upload;
import de.tarent.maven.plugins.pkg.Utils;
import de.tarent.maven.plugins.pkg.WorkspaceSession;
import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class RepReproDeployerTest extends AbstractMvnPkgPluginTestCase {

	PackageMap expectedPackageMap;
	String expectedRepo = "localrepo";
	WorkspaceSession ws;

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testConstructor() throws Exception {

		RepreproDeployer au = generateRepreproDeployer();

		String[] expectedCommand = new String[] {
				"reprepro",
				"-b" + expectedRepo,
				"include",
				"ubuntu_lucid",
				ws.getMojo().getBuildDir() + "/"
						+ ws.getHelper().getPackageFileNameWithoutExtension()
						+ ".changes" };
		String[] generatedCommand = au.generateCommand();

		Assert.assertEquals(expectedPackageMap.getPackaging(),
				(String) getValueOfFieldInObject("packagingType", au));
		Assert.assertEquals(expectedRepo,
				(String) getValueOfFieldInObject("repo", au));
		Assert.assertEquals(ws.getMojo().getLog(),
				(Log) getValueOfFieldInObject("l", au));
		Assert.assertEquals(ws.getMojo().getBuildDir(),
				(File) getValueOfFieldInObject("base", au));

		for (int i = 0; i < expectedCommand.length; i++) {
			Assert.assertEquals(expectedCommand[i], generatedCommand[i]);
		}
	}

	public RepreproDeployer generateRepreproDeployer() throws Exception {

		ws = new WorkspaceSession();
		expectedPackageMap = new PackageMap(null, null, "dull",
				new HashSet<String>());
		ws.setPackageMap(expectedPackageMap);
		Upload up = mockUploadEnvironment(UPLOADPOM);
		ws.setMojo(up);
		Helper h = new Helper();
		TargetConfiguration wantedTarget = null;
		Utils.mergeAllConfigurations(up.getTargetConfigurations());
		for (TargetConfiguration t : up.getTargetConfigurations()) {
			if (t.getTarget().equals("reprepro_upload")) {
				wantedTarget = t;
			}
		}
		h.init(up, expectedPackageMap, wantedTarget, null, "ubuntu_lucid");
		ws.setHelper(h);
		return new RepreproDeployer(ws, expectedRepo);

	}

	private Upload mockUploadEnvironment(String pomFilename) throws Exception {
		return (Upload) mockEnvironment(pomFilename, "upload", true,
				"reprepro_upload");
	}

	private Object getValueOfFieldInObject(String needle, Object obj)
			throws IllegalArgumentException, IllegalAccessException {

		Field[] allFields = RepreproDeployer.class.getDeclaredFields();

		for (int i = 0; i < allFields.length; i++) {
			if (allFields[i].getName().equals(needle)) {
				allFields[i].setAccessible(true);
				return allFields[i].get(obj);
			}
		}

		return null;
	}

}
