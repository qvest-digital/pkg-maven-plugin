package de.tarent.maven.plugins.pkg.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.AuxFile;

public class SPECFileGeneratorTest {

	SPECFileGenerator specgenerator;
	File spec;
	File dummytestscript;

	@Before
	public void setUp() throws IOException {
		specgenerator = new SPECFileGenerator();
		spec = File.createTempFile("SPECFileGeneratorTest","out");
		dummytestscript = File.createTempFile("dummytestscript",null);
	}

	@Test
	public void testFileGenerationWithParameters() throws IOException,
			MojoExecutionException {

		specgenerator.setArch("noarch");
		specgenerator.setSummary("Short dummy summary");
		specgenerator.setDependencies("dependency1,dependency2");
		specgenerator
				.setDescription("This is a long description\nwith linebreaks!");
		specgenerator.setGroup("DummyGroup/Tests");
		specgenerator.setUrl("http://dummyurl.com");
		specgenerator.setLicense("GPL");
		specgenerator.setPackageName("dummypackage");
		specgenerator.setVersion("v2");
		specgenerator.setLicense("huhu");
		specgenerator.setRelease("haha");
		specgenerator.generate(spec);

		Assert.assertTrue(spec.exists());
	}

	@Test(expected = MojoExecutionException.class)
	public void testFileGenerationWithPackageNameMissing()
			throws MojoExecutionException, IOException {
		specgenerator.generate(spec);
	}

	@Test
	public void testCreatePreinstallScriptFromExternalFileSuccessfully()
			throws IOException {
		createDummyTestScript();
		specgenerator.setPreinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
	}

	@Test(expected = IOException.class)
	public void testCreatePreinstallScriptFromExternalFileNotFoundThrowsException()
			throws IOException {
		specgenerator.setPreinstallcommandsFromFile(dummytestscript,
				dummytestscript.getName());
	}

	@Test
	public void testWriteFilesSection() throws MojoExecutionException,
			IOException {

		List<AuxFile> files = new ArrayList<AuxFile>();
		
		AuxFile testfile1 = new AuxFile("/file1");
		
		AuxFile testfile2 = new AuxFile("/file2");
		testfile2.setOwner("user");
		testfile2.setGroup("user");
		testfile2.setUserRead(true);
		testfile2.setGroupRead(true);
		testfile2.setOthersRead(true);
		
		files.add(testfile1);
		files.add(testfile2);	

		specgenerator.setFiles(files);		
		generateSpecWithMinimumInfo();

		String lookup1 = "%files";
		String lookup2 = "%defattr(755,root,root)";
		String lookup3 = "/file1";
		String lookup4 = "%attr(444,user,user) /file2";

		Assert.assertTrue("Could not find string", filecontains(lookup1));
		Assert.assertTrue("Could not find string", filecontains(lookup2));
		Assert.assertTrue("Could not find string", filecontains(lookup3));
		Assert.assertTrue("Could not find string", filecontains(lookup4));

	}
	
	@Test
	public void testWriteCleanCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample clean command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setCleancommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%clean";
		
		Assert.assertEquals(commands, specgenerator.getCleancommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
	}
	
	@Test
	public void testWriteBuildCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample build command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setBuildcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%build";
		
		Assert.assertEquals(commands, specgenerator.getBuildcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}
	
	@Test
	public void testWriteInstallCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample install command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setInstallcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%install";
		
		Assert.assertEquals(commands, specgenerator.getInstallcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}
	
	@Test
	public void testWritePreInstallCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample preinstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setPreinstallcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%pre";		
		Assert.assertEquals(commands, specgenerator.getPreinstallcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}
	
	@Test
	public void testWritePostInstallCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample postinstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setPostinstallcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%post";
		
		Assert.assertEquals(commands, specgenerator.getPostinstallcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}
	
	@Test
	public void testWritePreUnInstallCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample preuninstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setPreuninstallcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%preun";
		
		Assert.assertEquals(commands, specgenerator.getPreuninstallcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}
	
	@Test
	public void testWritePostUnInstallCommandsSection() throws MojoExecutionException, IOException{
		
		String command = "sample postuninstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		
		specgenerator.setPostuninstallcommands(commands);
		generateSpecWithMinimumInfo();
		
		String header = "%postun";
		
		Assert.assertEquals(commands, specgenerator.getPostuninstallcommands());
		Assert.assertTrue(filecontains(header));
		Assert.assertTrue(filecontains(command));
		
		
	}

	@After
	public void tearDown() {
		spec.delete();
		if (dummytestscript.exists()) {
			dummytestscript.delete();
		}
	}
	
	private void generateSpecWithMinimumInfo() throws MojoExecutionException, IOException{
		
		specgenerator.setArch("noarch");
		specgenerator.setSummary("Short dummy summary");
		specgenerator.setDependencies("dependency1,dependency2");
		specgenerator
				.setDescription("This is a long description\nwith linebreaks!");
		specgenerator.setGroup("DummyGroup/Tests");
		specgenerator.setUrl("http://dummyurl.com");
		specgenerator.setLicense("GPL");
		specgenerator.setPackageName("dummypackage");
		specgenerator.setVersion("v2");
		specgenerator.setLicense("huhu");
		specgenerator.setRelease("haha");
		specgenerator.generate(spec);
		
	}

	private boolean filecontains(String lookup) throws IOException {
		FileInputStream fis = new FileInputStream(spec);

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

	private void createDummyTestScript() throws FileNotFoundException {
		PrintWriter p = new PrintWriter(dummytestscript);
		p.println("Test command 1");
		p.println("Test command 2");
		p.println("Test command 3");
		p.close();

	}

}
