package de.tarent.maven.plugins.pkg;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MvnPkgPluginPackagingTest extends AbstractMvnPkgPluginTestCase {
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
	}
	
	@After
	public void tearDown() throws Exception{
		super.tearDown();
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
    public void runDefaultTarget()
            throws Exception
        {	
    		packagingPlugin = mockPackagingEnvironment("simplepom.xml", "pkg", "ubuntu_lucid_target_simple");
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
            assertTrue(debContainsCopyrightFile());
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
    public void createDebForUbuntuLucidWithoutDependenciesContainingJar()
            throws Exception
        {	
			packagingPlugin = mockPackagingEnvironment("simplepom.xml", "pkg", "ubuntu_lucid_target_simple");
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
        }
	
	/**
	 * This test attempts the following:
	 * 
	 * Execute a target configuration for RPM without license
	 * 
	 * This test should fail. 
	 * 
	*/
	@Test (expected=MojoExecutionException.class)
    public void createRpmForCentOS_5_6WithoutLicense()
            throws Exception
        {	

			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg","centos_5_6_target_simple");
			packagingPlugin.project.setLicenses(null);
			packagingPlugin.execute();
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
    public void createRpmForCentOS_5_6WithoutDependenciesContainingJar()
            throws Exception
        {	

			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg","centos_5_6_target_simple");
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
    public void createDebForUbuntuLucidWithManualDependenciesContainingJar()
            throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg","ubuntu_lucid_target_manual_dependencies");
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
    public void createRpmForCentOS_5_6WithManualDependenciesContainingJar()
            throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg","centos_5_6_target_manual_dependencies");
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
	@Test (expected=MojoExecutionException.class)
    public void createDebForUbuntuKarmicOverridingDistroWithoutDependenciesContainingJarShouldFail() throws Exception
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml", "pkg", "ubuntu_lucid_target_double");
            packagingPlugin.execute();
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
	@Test
    public void executingMultipleTargetsWithoutDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg",
														"ubuntu_lucid_target_simple,centos_5_6_target_simple");
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
	@Test
    public void createRPMWithAuxFileDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml","pkg","centos_5_6_target_external_artifact");
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
	@Test
    public void createDEBWithAuxFileDependenciesContainingJar()
            throws Exception, MojoExecutionException
        {
			packagingPlugin = mockPackagingEnvironment("simplepom.xml", "pkg","ubuntu_lucid_target_external_artifact");
            packagingPlugin.execute();
            assertTrue(numberOfDEBsIs(1));
            assertTrue(debContainsMainArtifact());
            assertTrue(debContainsArtifact("dummy.properties"));
        }	

		
	public Packaging mockPackagingEnvironment(String pomFilename, String goal) throws Exception{		
		 return (Packaging)mockEnvironment(pomFilename,"pkg");		
	}
	
	
	public Packaging mockPackagingEnvironment(String pomFilename, String goal, String target) throws Exception{		
		Packaging p = (Packaging) mockEnvironment(pomFilename,"pkg");
		p.target = target;
		return p;		
	}	
	
}
