package de.tarent.maven.plugins.pkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.helper.RpmHelper;

public class RPMHelperTest extends AbstractMvnPkgPluginTestCase{
	Packaging packaging;
	RpmHelper rpmPackageHelper;
	TargetConfiguration targetConfiguration;
	boolean previousfilefound;
	String homedir = System.getProperty("user.home");
	File f = new File(homedir + "/.rpmmacros");
	File buildDir = new File("src/test/resources/target");
	String ignorePackagingTypes = "pom";
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
		packaging = (Packaging) mockEnvironment("simplepom.xml","pkg");		
		targetConfiguration = new TargetConfiguration();
		targetConfiguration.setChosenTarget("");
		rpmPackageHelper = new RpmHelper(targetConfiguration, packaging);
		previousfilefound = false;
		if(f.exists()){
			FileUtils.moveFile(f, new File(homedir + "/.rpmmacros_Test_backup"));
			previousfilefound = true;
		}		
	}

	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
		if(previousfilefound){
			f.delete();
			FileUtils.moveFile(new File(homedir + "/.rpmmacros_Test_backup"),f);
		}
		
	}
	
	@Test
	public void testCreatingRpmmacrosfileWithoutMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
		
		rpmPackageHelper.setBasePkgDir(new File("/"));		
		rpmPackageHelper.createRpmMacrosFile();
		Assert.assertTrue("File not found",f.exists());
		rpmPackageHelper.restoreRpmMacrosFileBackup(null);
	}
	
	@Test
	public void testCreatingRpmmacrosfileWitMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
		targetConfiguration.setMaintainer("Dummy maintainer");		
		rpmPackageHelper.setBasePkgDir(new File("/"));		
		rpmPackageHelper.createRpmMacrosFile();
		Assert.assertTrue(f.exists());
		Assert.assertTrue("String not found", filecontains(f, "%_gpg_name       Dummy maintainer"));
		rpmPackageHelper.restoreRpmMacrosFileBackup(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreatingRpmmacrosfileWithoutBaseDirThrowsException() throws IOException, MojoExecutionException{

		try{rpmPackageHelper.createRpmMacrosFile();}catch(Exception e){Assert.assertTrue(true);}		
		
	}
	
	@Test
	public void testPrepareInitialDirectoriesScuccesfully() throws MojoExecutionException{
		File tempRoot = new File("/tmp/BaseTestTemp");
		File base = new File("/tmp/BaseTestTemp/Base");
		rpmPackageHelper.setBasePkgDir(base);
		rpmPackageHelper.setTempRoot(tempRoot);
		rpmPackageHelper.prepareInitialDirectories();
		Assert.assertTrue(new File("/tmp/BaseTestTemp/Base").exists());
		Assert.assertEquals(new File(rpmPackageHelper.getBasePkgDir(),"/BUILD"),rpmPackageHelper.getBaseBuildDir());
		Assert.assertEquals(new File(rpmPackageHelper.getBasePkgDir(),"/SPECS"),rpmPackageHelper.getBaseSpecsDir());		
		base.delete();
		
	}
	
	@Test(expected=NullPointerException.class)
	public void testPrepareInitialDirectoriesWithoutBaseOrTempRootThrowsException() throws MojoExecutionException{
		try{rpmPackageHelper.prepareInitialDirectories();}catch(Exception e){Assert.assertTrue(true);}
	}
	@Test
	public void testGenerateFilelist() throws MojoExecutionException, IOException{
		File tempdir = File.createTempFile("temp", "file");
		rpmPackageHelper.setBaseBuildDir(tempdir.getParentFile());
		Assert.assertTrue(rpmPackageHelper.generateFilelist().size()>0);

	}
	
	@Test
	public void testGetVersionReturnsPackagingVersion(){
		Assert.assertTrue(rpmPackageHelper.getVersion().contains(packaging.project.getVersion()));
	}
	
	@Test
	public void testGetDstArtifactFileReturnsgetBaseBuildDirAndgetTargetArtifactFiletoStringIfNotSet(){
		File testTempdir = new File("/tmp/BaseTestTemp");
		File testArtifactfile = new File("file1");
		rpmPackageHelper.setBaseBuildDir(testTempdir);
		rpmPackageHelper.setTargetArtifactFile(testArtifactfile);
		Assert.assertEquals(rpmPackageHelper.getBaseBuildDir()+"/"+rpmPackageHelper.getTargetArtifactFile().toString(),rpmPackageHelper.getDstArtifactFile().toString());
		
	}
	
	private boolean filecontains(File file, String lookup) throws IOException {
		FileInputStream fis = new FileInputStream(file);

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			try{
				String currentLine = "";
				while ((currentLine = in.readLine()) != null) {
					if (currentLine.indexOf(lookup) != -1)
						return true;
				}
			}finally{
				IOUtils.closeQuietly(in);
			}
		} finally {
			IOUtils.closeQuietly(fis);		
		}
		return false;
	}
}
