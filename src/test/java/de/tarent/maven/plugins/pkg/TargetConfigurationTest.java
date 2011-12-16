package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;

import junit.framework.TestCase;

public class TargetConfigurationTest extends TestCase {

	/**
	 * Tests whether the JNI file sets are really merged.
	 * @throws MojoExecutionException 
	 */
	public void testJNIFileMerge() throws MojoExecutionException
	{
		JniFile f;
		List<JniFile> expected = new ArrayList<JniFile>();
		
		TargetConfiguration tc1 = new TargetConfiguration();
		List<JniFile> l1 = new ArrayList<JniFile>();
		
		f = new JniFile();
		f.from = "bla";
		f.to = "blu/";
		expected.add(f);
		
		l1.add(f);
		
		tc1.setJniFiles(l1);
		
		TargetConfiguration tc2 = new TargetConfiguration();
		List<JniFile> l2 = new ArrayList<JniFile>();
		
		f = new JniFile();
		f.from = "foo";
		f.to = "bar/";
		expected.add(f);
		
		l2.add(f);
		tc2.setJniFiles(l2);
		
		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
		assertEquals(expected, merged.getJniFiles());
	}
	
	/**
	 * Tests whether the distros property is really and properly merged.
	 * @throws MojoExecutionException 
	 */
	public void testDistrosMerge() throws MojoExecutionException
	{
		String d;
		Set<String> expected = new HashSet<String>();
		
		TargetConfiguration tc1 = new TargetConfiguration();
		Set<String> l1 = new HashSet<String>();
		
		d = "ubuntu_jaunty";
		expected.add(d);
		l1.add(d);
		
		tc1.setDistros(l1);
		
		TargetConfiguration tc2 = new TargetConfiguration();
		Set<String> l2 = new HashSet<String>();
		
		d = "debian_lenny";
		expected.add(d);
		l2.add(d);
		
		tc2.setDistros(l2);
		
		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
		assertEquals(expected, merged.getDistros());
	}

	/**
	 * Tests whether the distros property is really and properly merged.
	 * @throws MojoExecutionException 
	 */
	public void testSystemPropertiesMerge() throws MojoExecutionException
	{
		Properties expected = new Properties();
		
		TargetConfiguration tc1 = new TargetConfiguration();
		Properties l1 = new Properties();
		
		expected.setProperty("bla", "blavalue");
		l1.setProperty("bla", "blavalue");
		tc1.setSystemProperties(l1);
		
		TargetConfiguration tc2 = new TargetConfiguration();
		Properties l2 = new Properties();
		
		expected.setProperty("foo", "foovalue");
		l2.setProperty("foo", "foovalue");
		
		tc2.setSystemProperties(l2);
		
		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
		assertEquals(expected, merged.getSystemProperties());
	}

	/**
	 * Tests whether the manual dependencies property is really
	 * and properly merged.
	 * @throws MojoExecutionException 
	 */
	public void testManualDependenciesMerge() throws MojoExecutionException
	{
		String d;
		List<String> expected = new ArrayList<String>();
		
		TargetConfiguration tc1 = new TargetConfiguration();
		List<String> l1 = new ArrayList<String>();
		
		d = "foo";
		expected.add(d);
		l1.add(d);
		
		tc1.setManualDependencies(l1);
		
		TargetConfiguration tc2 = new TargetConfiguration();
		List<String> l2 = new ArrayList<String>();
		
		d = "bar";
		expected.add(d);
		l2.add(d);
		
		tc2.setManualDependencies(l2);
		
		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
		assertEquals(expected, merged.getManualDependencies());
	}
	
	/**
	 * Tests if a target configuration is ready to be used.
	 * It should only be ready when it has been fixated or merged at least once.
	 * @throws MojoExecutionException 
	 */
	public void testTargetConfigurationIsReady() throws MojoExecutionException{
		TargetConfiguration tc = new TargetConfiguration();
		assertFalse(tc.isReady());
		tc.fixate();
		assertTrue(tc.isReady());
		tc = new TargetConfiguration();
		assertFalse(tc.isReady());
		Utils.mergeConfigurations(tc,new TargetConfiguration());
		assertTrue(tc.isReady());
		
	}
}
