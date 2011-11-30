package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.testingstubs.PkgArtifactStub;
import de.tarent.maven.plugins.pkg.testingstubs.PkgProjectStub;

public class MvnPkgPluginTest extends AbstractMojoTestCase {

	Packaging packagingPlugin;
	
	/**{@inheritDoc} */
	protected void setUp() throws Exception{
		super.setUp();
		FileUtils.cleanDirectory(new File(getBasedir()+ "/src/test/resources/dummyproject/target/"));
		
	}

	/**{@inheritDoc} */	
	protected void tearDown()throws Exception{
		super.tearDown();
		FileUtils.cleanDirectory(new File(getBasedir()+ "/src/test/resources/dummyproject/target/"));
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
    		packagingPlugin.target = "ubuntu_lucid_target_simple";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
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
    public void testCreateRpmForCentOS_5_6WithDependenciesContainingJar()
            throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "centos_5_6_target_manual_dependencies";
            packagingPlugin.execute();
            assertTrue(numberOfRPMsIs(1));

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
	 * 
	 * @throws Exception
	 */
	@Test(expected=MojoExecutionException.class)
    public void testCreateDebAndRPMWithoutDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml");
    		packagingPlugin.target = "ubuntu_lucid_target_simple,centos_5_6_target_simple";
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(numberOfRPMsIs(1));
        }	
	
	/**
	 * This method mocks the packaging environment. It loads an external pom, initialites the mvn-pkg-plugin
	 * and sets enough information for basic tests to succeed. It can then be manipulated to achieve more complex
	 * testing.
	 *  
	 * @param pom An external pom file containing at least the plugin section refferring to mvn-pkg-plugin 
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
        
        
        packagingPlugin.buildDir =  new File(getBasedir(), "src/test/resources/dummyproject/target/" );
        packagingPlugin.outputDirectory = new File(getBasedir(), "src/test/resources/dummyproject/target/" );
        
        //Create artifact stub, as we wont actually compile anything 
		File f = new File(getBasedir()+ "/src/test/resources/dummyproject/target/" +
				 packagingPlugin.finalName +  "." + 
				 packagingPlugin.project.getPackaging());
		f.createNewFile();
		PkgArtifactStub artifactStub = new PkgArtifactStub(f);	
        packagingPlugin.artifact =	 artifactStub;
        
        return packagingPlugin;
		
	} 
	
	public File[] returnFilesFoundBasedOnSuffix(String suffix){
		
		final Pattern p = Pattern.compile(".*\\." + suffix);
	    return new File(getBasedir()+ "/src/test/resources/dummyproject/target/").listFiles(new FileFilter() {			
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
	
}
