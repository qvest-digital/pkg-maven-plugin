package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.testingstubs.PkgArtifactStub;
import de.tarent.maven.plugins.pkg.testingstubs.PkgProjectStub;

public class MvnPkgPluginTest extends AbstractMojoTestCase {

	Packaging packagingPlugin;
	private static final File TARGETDIR = new File(getBasedir()+ "/src/test/resources/dummyproject/target/");
	
	/**{@inheritDoc} */
	protected void setUp() throws Exception{
		super.setUp();
		FileUtils.cleanDirectory(TARGETDIR);
		
	}

	/**{@inheritDoc} */	
	protected void tearDown()throws Exception{
		super.tearDown();
		FileUtils.cleanDirectory(TARGETDIR);
	}
	
	/**
	 * This test attempts the following:
	 * 
	 * Execute the default target set on the configuration section of the pom file (ubuntu_lucid_target_simple)
	 * Use the only distribution defined for this target
	 * Create a DEB file 
	 * Include a main artifact (JAR) in the DEB file 
	 * 
	 * @throws Exception
	 */
	@Test
    public void testRunDefaultTarget()
            throws Exception
        {	
    		packagingPlugin = mockPackagingEnvironment("simplepom.xml");    		
    		packagingPlugin.target = "ubuntu_lucid_target_simple";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
        }	
	


	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration which itself has parent configuration
	 * Use the only distribution defined for this target
	 * Create a DEB file 
	 * Include a main artifact (JAR) in the DEB file 
	 * 
	 * @throws Exception
	 */
	@Test
    public void testCreateDebForUbuntuLucidWithoutDependenciesContainingJar()
            throws Exception
        {	
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_simple";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
        }
	
	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration without parent
	 * Use the only distribution defined for this target
	 * Create a RPM file 
	 * Include a main artifact (JAR) in the RPM file 
	 * 
	*/
	@Test
    public void testCreateRpmForCentOS_5_6WithoutDependenciesContainingJar()
            throws Exception
        {	

			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "centos_5_6_target_simple";  
            packagingPlugin.execute();
            assertTrue(numberOfRPMsIs(1));
            assertTrue(rpmContainsMainArtifact());
        }
	
	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration 
	 * 		which itself has parent configuration
	 * 		which contains manually added dependencies
	 * Use the only distribution defined for this target  
	 * Create a DEB file 
	 * Include a main artifact (JAR) in the DEB file 
	 * 
	 * @throws Exception
	 */
	@Test
    public void testCreateDebForUbuntuLucidWithManualDependenciesContainingJar()
            throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_manual_dependencies";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
            assertTrue(debDependsOn("blackbox"));
        }

	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration 
	 * 		which contains manually added dependencies
	 * Use the only distribution defined for this target
	 * 
	 * Create a RPM file 
	 * Include a main artifact (JAR) in the RPM file 
	 * 
	 * @throws Exception
	 */	
	@Test
    public void testCreateRpmForCentOS_5_6WithManualDependenciesContainingJar()
            throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "centos_5_6_target_manual_dependencies";
            packagingPlugin.execute();
            assertTrue(numberOfRPMsIs(1));
            assertTrue(rpmContainsMainArtifact());
            assertTrue(rpmDependsOn("blackbox"));
        }

	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration 
	 * 		which itself has parent configuration
	 * 		which contains two distributions, but no default
	 * 
	 * Create a DEB file 
	 * Include a main artifact (JAR) in the DEB file 
	 * 
	 * This test should fail.
	 * 
	 * @throws Exception
	 */
	@Test(expected=MojoExecutionException.class)
    public void testCreateDebForUbuntuKarmicOverridingDistroWithoutDependenciesContainingJarShouldFail()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_double";
    		assertNotNull( packagingPlugin );            
            try{
            	packagingPlugin.execute();
            }catch(MojoExecutionException ex){
            	/* This is just silly: AbstractMojoTestCase extends TestCase, which wont allow
            	 * expected=MojoExecutionException.class to work, so we will just catch the error */
            	assertTrue(ex.toString().contains("more than one distro is supported"));
            }
        }

	/**
	 * This test attempts the following:
	 * 
	 * Execute two different target configurations 
	 * Create a DEB and a RPM file 
	 * Include a main artifact (JAR) in the DEB file
	 * Include a main artifact (JAR) in the RPM file
	 * 
	 * @throws Exception
	 */
	@Test(expected=MojoExecutionException.class)
    public void testExecutingMultipleTargetsWithoutDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_simple,centos_5_6_target_simple";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(numberOfRPMsIs(1));
            assertTrue(rpmContainsMainArtifact());
            assertTrue(debContainsMainArtifact());
        }	

	/**
	 * This test attempts the following:
	 * 
	 * Execute two different target configurations 
	 * Create a DEB and a RPM file 
	 * Include a main artifact (JAR) in the DEB file
	 * Include a main artifact (JAR) in the RPM file
	 * 
	 * @throws Exception
	 */
	@Test(expected=MojoExecutionException.class)
    public void testRPMWithAuxFileDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "centos_5_6_target_external_artifact";
            packagingPlugin.execute();
            assertTrue(numberOfRPMsIs(1));
            assertTrue(rpmContainsMainArtifact());
            assertTrue(rpmContainsArtifact("dummy.properties"));
        }
	
	/**
	 * This test attempts the following:
	 * 
	 * Execute two different target configurations 
	 * Create a DEB and a RPM file 
	 * Include a main artifact (JAR) in the DEB file
	 * Include a main artifact (JAR) in the RPM file
	 * 
	 * @throws Exception
	 */
	@Test(expected=MojoExecutionException.class)
    public void testDEBWithAuxFileDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_external_artifact";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
            assertTrue(debContainsArtifact("dummy.properties"));
        }	
	
	/**
	 * This method mocks the packaging environment. It loads an external pom, initialites the mvn-pkg-plugin
	 * and sets enough information for basic tests to succeed. It can then be manipulated to achieve more complex
	 * testing.
	 *  
	 * @param pom An external pom file containing at least the plugin section refferring to mvn-pkg-plugin. The 
	 * file should be tored under src/test/resources/dummyproject/
	 * @return
	 * @throws Exception
	 */
	public Packaging mockPackagingEnvironment(String pomFilename) throws Exception{
		

        File pom = getTestFile( getBasedir(), "src/test/resources/dummyproject/" + pomFilename );
		// Create plugin based on the external pom file
		Packaging packagingPlugin = (Packaging) lookupMojo("pkg", pom);
        packagingPlugin.setPluginContext(new HashMap());       
        
        // Create a project contained by the plugin based on the external pom file 
        packagingPlugin.project = new PkgProjectStub(pom);

        // Parameters that are not part of the mvn-pkg-plugin section are somehow loaded into the project
        // TODO: Find why this problem exists and/or a more elegant way to do this        
        packagingPlugin.project.setPackaging("jar");
        packagingPlugin.project.setName("DummyProject");
        packagingPlugin.project.setArtifactId("DummyProject");
        packagingPlugin.project.setDescription("DummyDescription");
        packagingPlugin.project.setUrl("http://DummyURL.com");
        packagingPlugin.version =    packagingPlugin.project.getVersion();
        packagingPlugin.artifactId = packagingPlugin.project.getArtifactId();
        packagingPlugin.finalName =	 packagingPlugin.project.getArtifactId();
        
        
        packagingPlugin.buildDir =  TARGETDIR;
        packagingPlugin.outputDirectory = TARGETDIR;
        
        //Create artifact stub, as we wont actually compile anything 
		File f = new File(TARGETDIR +"/"+
				 packagingPlugin.finalName +  "." + 
				 packagingPlugin.project.getPackaging());
		f.createNewFile();
		PkgArtifactStub artifactStub = new PkgArtifactStub(f);	
        packagingPlugin.artifact =	 artifactStub;
        
        return packagingPlugin;
		
	} 
	
	public File[] returnFilesFoundBasedOnSuffix(String suffix){
		
		final Pattern p = Pattern.compile(".*\\." + suffix);
	    return TARGETDIR.listFiles(new FileFilter() {			
			@Override
	        public boolean accept(File file) {
	            return p.matcher(file.getName()).matches();
			}
		});		
	}

	private boolean numberOfRPMsIs(int i) {
		return returnFilesFoundBasedOnSuffix("rpm").length==i;
	}

	private boolean numberOfDEBsIs(int i) {
		return returnFilesFoundBasedOnSuffix("deb").length==i;
	}
	
	private boolean debContains(Pattern p, String debArgs) throws MojoExecutionException, IOException{
		boolean result = false;
		String out = IOUtils.toString(Utils.exec(new String[]{"dpkg",debArgs,
				returnFilesFoundBasedOnSuffix("deb")[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		Log l = packagingPlugin.getLog();
		l.info("Matching" + out + "/// to "+p);
		if (p.matcher(out).find()){
			result = true;
		}
		return result;
	}
	
	private boolean rpmContains(Pattern p, String rpmArgs) throws MojoExecutionException, IOException{
		boolean result = false;
		String out = IOUtils.toString(Utils.exec(new String[]{"rpm","-pq",rpmArgs,
				returnFilesFoundBasedOnSuffix("rpm")[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		if (p.matcher(out).find()){
			result = true;
		}		
		return result;
	}
	
	private boolean debContainsMainArtifact() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+
										  Pattern.quote(packagingPlugin.artifact.getFile().getName())+
										  ".*");
		return debContains(p, "-c");		
	}
	
	private boolean rpmContainsMainArtifact() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+
										  Pattern.quote(packagingPlugin.artifact.getFile().getName())+
										  ".*");
		return rpmContains(p,"--dump");
	}
	
	private boolean rpmContainsArtifact(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+Pattern.quote(s)+".*");
		return rpmContains(p,"--dump");
	}
	
	private boolean debContainsArtifact(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+ Pattern.quote(s)+ ".*");
		return debContains(p, "-c");		
	}
		
	private boolean rpmDependsOn(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(Pattern.quote(s)+".*");
		return rpmContains(p,"-R");
	}	

	private boolean debDependsOn(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile("Depends:.*"+Pattern.quote(s)+".*");
		return debContains(p, "--info");
	}
	
	
}
