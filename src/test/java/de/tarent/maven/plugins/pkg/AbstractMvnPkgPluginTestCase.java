package de.tarent.maven.plugins.pkg;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

import de.tarent.maven.plugins.pkg.testingstubs.PkgArtifactStub;
import de.tarent.maven.plugins.pkg.testingstubs.PkgProjectStub;

@RunWith(JUnit4ClassRunner.class)
public abstract class AbstractMvnPkgPluginTestCase extends AbstractMojoTestCase {

	Packaging packagingPlugin;
	Upload packagingTransportPlugin;
	protected static final File TARGETDIR = new File(getBasedir()+ "/src/test/resources/dummyproject/target/");

	/**
	 * Name of the pom used for .deb packaging tests 
	 */
	protected static final String DEBPOM = "debpom.xml";
	/**
	 * Name of the pom used for .rpm packaging tests
	 */
	protected static final String RPMPOM = "rpmpom.xml";
	/**
	 * Name of the pom used for .deb packaging tests 
	 */
	protected static final String IPKPOM = "ipkpom.xml";
	/**
	 * Name of the pom used for .deb packaging tests 
	 */
	protected static final String IZPACKPOM = "izpackpom.xml";
	/**
	 * Name of the pom used for mixed packaging tests
	 */
	protected static final String MIXEDPOM = "mixedpom.xml";
	/**
	 * Name of the pom used for upload tests
	 */
	protected static final String UPLOADPOM = "uploadpom.xml";
	/**
	 * This is the key fingerprint for Test User MVNPKGPLUGIN <no@address.com></br>
	 * It is needed for test purposes
	 */
	protected static String keyFingerprint = "A70F93982E429501732931CF0481A82949692090";
	/**
	 * This is the keyID for Test User MVNPKGPLUGIN <no@address.com> ()</br>
	 * (The last four hexadecimal character groups of the user's fingerprint). 
	 * It is needed for test purposes
	 */
	protected static String keyID;
	/**
	 * location of the public key to use
	 */
	protected static String PUBLICKEYLOCATION;
	/**
	 * location of the private key to use
	 */
	protected static String PRIVATEKEYLOCATION;
	/**
	 * This enum is used to define the type of keys to be used when signing packages.
	 * @author plafue
	 *
	 */
	private enum KeyType{
		RSA,
		DSA
	}
	/**
	 * Flag to determine if the target directory should be emptied
	 * after running each test
	 */
	protected static boolean CLEANTARGETDIRECTORYAFTERRUN = true;
	/**
	 * Flag to determine if the target directory should be emptied
	 * before running each test
	 */
	protected static boolean CLEANTARGETDIRECTORYBEFORERUN = true;
	
	@BeforeClass
	public static void keySetup() throws MojoExecutionException{
		// We will add keys for Test User once per session with these methods
		selectKeyForRPMVersion();
		addTestGPGKey();
	}

	
	@AfterClass
	public static void keyRemoval(){		
		// Cleaning up imported GPG keys
		try{
			removeTestGPGKey();
		}catch (Exception e) {
			// Nothing to do here
		}
	}
	
	/**{@inheritDoc} */
	protected void setUp() throws Exception{
		super.setUp();
		FileUtils.forceMkdir(TARGETDIR);
		if (CLEANTARGETDIRECTORYBEFORERUN) {
			FileUtils.cleanDirectory(TARGETDIR);
		}
		
	}

	/**{@inheritDoc} */	
	protected void tearDown()throws Exception{
		super.tearDown();
		if (CLEANTARGETDIRECTORYAFTERRUN) {
			FileUtils.cleanDirectory(TARGETDIR);
		}
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
	public AbstractPackagingMojo mockEnvironment(String pomFilename, String goal, boolean setAllNeededParameters) 
			throws Exception{
		

        File pom = getTestFile( getBasedir(), "src/test/resources/dummyproject/" + pomFilename );
		// Create plugin based on the external pom file
        AbstractPackagingMojo packagingPlugin = (AbstractPackagingMojo) lookupMojo(goal, pom);
        packagingPlugin.setPluginContext(new HashMap<String,String>());       
        
        // Create a project contained by the plugin based on the external pom file 
        packagingPlugin.project = new PkgProjectStub(pom);

        // Parameters that are not part of the mvn-pkg-plugin section are somehow loaded into the project
        // TODO: Find why this problem exists and/or a more elegant way to do this
        
        if(setAllNeededParameters){
        	setNeededInformation(packagingPlugin);
        }
        
        packagingPlugin.buildDir =  TARGETDIR;
        packagingPlugin.outputDirectory = TARGETDIR;
        
        // Workaround for a bug (maven does not load default-value parameters:
        // http://maven.40175.n5.nabble.com/default-value-are-not-injected-td3907553.html
        
        packagingPlugin.ignorePackagingTypes = "pom";
        
        //Create artifact stub, as we wont actually compile anything 
		File f = new File(TARGETDIR +"/"+
				 packagingPlugin.finalName +  "." + 
				 packagingPlugin.project.getPackaging());
		f.createNewFile();
		PkgArtifactStub artifactStub = new PkgArtifactStub(f);	
        packagingPlugin.project.setArtifact(artifactStub);
        
        return packagingPlugin;
		
	}
	public AbstractPackagingMojo mockEnvironment(String pomFilename, String goal) throws Exception{
		return mockEnvironment(pomFilename,goal,true);
	}


	private void setNeededInformation(AbstractPackagingMojo packagingPlugin) {
		packagingPlugin.project.setPackaging("jar");
        packagingPlugin.project.setName("DummyProject");
        packagingPlugin.project.setArtifactId("DummyProject");
        packagingPlugin.project.setDescription("DummyDescription");
        packagingPlugin.project.setUrl("http://DummyURL.com");
        packagingPlugin.project.setVersion("1.0.0");
        packagingPlugin.project.setLicenses(createLicenseList("License 1","License 2"));
        packagingPlugin.version =    packagingPlugin.project.getVersion();
        packagingPlugin.artifactId = packagingPlugin.project.getArtifactId();
        packagingPlugin.finalName =	 packagingPlugin.project.getArtifactId();
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
	
	public File[] returnFilesBasedOnFilename(String filename){
		
		final Pattern p = Pattern.compile(".*" + filename);
	    return TARGETDIR.listFiles(new FileFilter() {			
			@Override
	        public boolean accept(File file) {
	            return p.matcher(file.getName()).matches();
			}
		});		
	}

	protected boolean numberOfRPMsIs(int i) {
		return returnFilesFoundBasedOnSuffix("rpm").length==i;
	}

	protected boolean numberOfDEBsIs(int i) {
		return returnFilesFoundBasedOnSuffix("deb").length==i;
	}

	protected boolean numberOfIPKsIs(int i) {
		return returnFilesFoundBasedOnSuffix("ipk").length==i;
	}
	
	private boolean debContains(Pattern p, String debArgs) throws MojoExecutionException{
		boolean result = false;
		String out = new String(); 
		try{
			out = IOUtils.toString(Utils.exec(new String[]{"dpkg",debArgs,
				returnFilesFoundBasedOnSuffix("deb")[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		}catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		}
		//Log l = packagingPlugin.getLog();
		//l.info("Matching" + out + "/// to "+p);
		if (p.matcher(out).find()){
			result = true;
		}
		return result;
	}

	private boolean debContains(Pattern p, String debArgs, String fileName) throws MojoExecutionException{
		boolean result = false;
		String out = new String(); 
		try{
			out = IOUtils.toString(Utils.exec(new String[]{"dpkg",debArgs,
				returnFilesBasedOnFilename(fileName)[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		}catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		}
		//Log l = packagingPlugin.getLog();
		//l.info("Matching" + out + "/// to "+p);
		if (p.matcher(out).find()){
			result = true;
		}
		return result;
	}

	private boolean ipkContains(Pattern p, String debArgs) throws MojoExecutionException{
		//TODO: Create a real ipk check
		boolean result = false;
		String out = new String(); 
		try{
			out = IOUtils.toString(Utils.exec(new String[]{"dpkg",debArgs,
				returnFilesFoundBasedOnSuffix("ipk")[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		}catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		}
		//Log l = packagingPlugin.getLog();
		//l.info("Matching" + out + "/// to "+p);
		if (p.matcher(out).find()){
			result = true;
		}
		return result;
	}
	
	private boolean rpmContains(Pattern p, String rpmArgs) throws MojoExecutionException{
		boolean result = false;
		String out = new String();
		try{
			out = IOUtils.toString(Utils.exec(new String[]{"rpm","-pq",rpmArgs,
				returnFilesFoundBasedOnSuffix("rpm")[0].getAbsolutePath()},TARGETDIR,
				"Failure checking contents", "Failure opening rpm file"));
		}catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		}
		
		if (p.matcher(out).find()){
			result = true;
		}		
		return result;
	}
	
	protected boolean debContainsMainArtifact() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+
										  Pattern.quote(packagingPlugin.project.getArtifact().getFile().getName())+
										  ".*");
		return debContains(p, "-c");		
	}
	
	protected boolean rpmContainsMainArtifact() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+
				  Pattern.quote(packagingPlugin.project.getArtifact().getFile().getName())+
				  ".*");
		return rpmContains(p,"--dump");
	}
	
	protected boolean ipkContainsMainArtifact() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+
										  Pattern.quote(packagingPlugin.project.getArtifact().getFile().getName())+
										  ".*");
		return ipkContains(p, "-c");		
	}
	
	protected boolean rpmIsSigned() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(keyID);
		return rpmContains(p,"-i");
	}
	
	protected boolean rpmContainsArtifact(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+Pattern.quote(s)+".*");
		return rpmContains(p,"--dump");
	}
	
	protected boolean debContainsArtifact(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+ Pattern.quote(s)+ ".*");
		return debContains(p, "-c");		
	}
	
	protected boolean ipkContainsArtifact(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(".*"+ Pattern.quote(s)+ ".*");
		return ipkContains(p, "-c");		
	}
		
	protected boolean rpmDependsOn(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile(Pattern.quote(s)+".*");
		return rpmContains(p,"-R");
	}	

	protected boolean debDependsOn(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile("Depends:.*"+Pattern.quote(s)+".*");
		return debContains(p, "--info");
	}	

	protected boolean ipkDependsOn(String s) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile("Depends:.*"+Pattern.quote(s)+".*");
		return ipkContains(p, "--info");
	}		

	protected boolean debDependsOn(String s, String filename) throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile("Depends:.*"+Pattern.quote(s)+".*");
		return debContains(p, "--info", filename);
	}
	
	protected List<License> createLicenseList(String ... strings)
	{
		List<License> licenses = new ArrayList<License>();
		for(int i=0;i<strings.length;i++){
			License l = new License();
			l.setName(strings[i]);
			l.setUrl("http://www.gnu.org/licenses/lgpl.txt");
			licenses.add(l);			
		}		
		return licenses;
	}

	protected boolean debContainsCopyrightFile() throws MojoExecutionException, IOException {
		final Pattern p = Pattern.compile("lines.*copyright");
		return debContains(p, "--info");
	}
	protected boolean debIsSigned() throws MojoExecutionException{
		Utils.exec(new String[]{"ar","x",returnFilesFoundBasedOnSuffix("deb")[0].getAbsolutePath()},
				   TARGETDIR,
			  	   "Error extracting package",
			  	   "Error extracting package");

		  File f = new File(TARGETDIR,"_gpgorigin");
		  if(f.exists()){
			  return true;
		  }else{
			  return false;
		  }
	}

	
	protected boolean filecontains(File file, String lookup) throws IOException {
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
	
	/**
	 * Sets the variables related to the GPG keys needed by some tests.
	 * 
	 * @throws MojoExecutionException
	 */
	private static void selectKeyForRPMVersion() throws MojoExecutionException {
		KeyType type = getKeyTypeForRPMVersion();
		PUBLICKEYLOCATION = "src/test/resources/testuserkeys/"+ type +"/testuser_public.key";
		PRIVATEKEYLOCATION = "src/test/resources/testuserkeys/"+ type +"/testuser_private.key";
		if(type==KeyType.DSA){
				keyFingerprint = "A70F93982E429501732931CF0481A82949692090";
		}else if(type==KeyType.RSA){
				keyFingerprint = "22D763EEFA5E27AE399357C3B18A1F8A9544944C";
		}			
		keyID = keyFingerprint.substring(24, 40).toLowerCase();
	}

	/**
	 * Returns an appropriate keytype to be used when importing keys for the test user.<br/>
	 * 
	 * <p>Older versions of rpm do not work as expected when using RSA keys 
	 * (<a href="https://bugzilla.redhat.com/show_bug.cgi?id=436812">Bug 436812</a> in RedHat's Bugzilla).
	 * This method evaluates the version of rpm being used and, if >=4.8 returns keytype RSA, otherwise DSA.<p> 
	 * @return
	 * @throws MojoExecutionException
	 */
	private static KeyType getKeyTypeForRPMVersion() throws MojoExecutionException {
		String version = null;
		try {
			version = IOUtils.toString(Utils.exec(new String[]{"rpm","--version"}, 
												  new File("/"),
												  "error getting rpm version",
												  "error getting rpm version"));
			
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		}
		Pattern p = Pattern.compile("\\s[4-9]\\.([8-9]|\\d{2})\\.?");
		Matcher m = p.matcher(version);
		if (m.find()){
			return KeyType.RSA;	
		}else{
			return KeyType.DSA;
		}
	}
	
	/**
	 * Imports the GPG key needed for the tests that sign packages.
	 * @throws MojoExecutionException
	 */
	private static void addTestGPGKey() throws MojoExecutionException{
		
		Utils.exec(new String[]{"gpg","--batch","--import",	PRIVATEKEYLOCATION,	PUBLICKEYLOCATION}, 
				"Error adding GPG key", 
				"Error writing GPG key");
		
	}
	
	/**
	 * Deletes the GPG key used by the tests that sign packages. 
	 * @throws MojoExecutionException
	 */
	private static void removeTestGPGKey() throws MojoExecutionException{
		Utils.exec(new String[]{"gpg","--batch","--delete-secret-and-public-keys",keyFingerprint}, 
								"Error removing GPG key", 
								"Error removing GPG key");
	}

}
