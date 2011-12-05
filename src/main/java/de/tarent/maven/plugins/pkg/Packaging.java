/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb, ipk, izpack)
 * Copyright (C) 2000-2008 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 11 March 2008
 * Elmar Geese, CEO tarent GmbH.
 */



package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.tarent.maven.plugins.pkg.helper.DebHelper;
import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.helper.IpkHelper;
import de.tarent.maven.plugins.pkg.helper.IzPackHelper;
import de.tarent.maven.plugins.pkg.helper.RpmHelper;
import de.tarent.maven.plugins.pkg.map.PackageMap;
import de.tarent.maven.plugins.pkg.packager.DebPackager;
import de.tarent.maven.plugins.pkg.packager.IpkPackager;
import de.tarent.maven.plugins.pkg.packager.IzPackPackager;
import de.tarent.maven.plugins.pkg.packager.Packager;
import de.tarent.maven.plugins.pkg.packager.RPMPackager;

/**
 * Creates a package file for the project and the given distribution.
 * 
 * @execute phase="package"
 * @goal pkg
 */
public class Packaging
    extends AbstractPackagingMojo
{

  private static final String DEFAULT_SRC_AUXFILESDIR = "src/main/auxfiles";

  protected TargetConfiguration dc;

  /**
   * @parameter
   * @required
   */
  protected TargetConfiguration defaults;

  /**
   * @parameter
   */
  protected List<TargetConfiguration> targetConfigurations;

  protected PackageMap pm;  

  public PackageMap getPm() {
	return pm;
  }
	
  public void setPm(PackageMap pm) {
	this.pm = pm;
  }

  public MavenProject getProject() {
	return project;
  }
  
  public String getIgnorePackagingTypes(){
	  return ignorePackagingTypes;
  }
  
  public File getBuildDir(){
	  return buildDir;
  }
  
  public File getOutputDirectory(){
	  return outputDirectory;
  }
  
  public String get_7zipExec(){
	  return _7zipExec;
  }
  
  public String getJavaExec(){
    return javaExec;
  }
  
  public String getFinalName(){
    return finalName;
  }


	public ArtifactMetadataSource getMetadataSource() {
		return metadataSource;
	}
	
	public ArtifactFactory getFactory() {
		return factory;
	}
	
	public ArtifactResolver getResolver() {
		return resolver;
	}
	
	public ArtifactRepository getLocal() {
		return local;
	}
	
	public List<TargetConfiguration> getTargetConfigurations() {
		return targetConfigurations;
	}
  
/**
   * Validates arguments and test tools.
   * 
   * @throws MojoExecutionException
   */
  void checkEnvironment(Log l) throws MojoExecutionException
  {
    l.info("distribution             : " + dc.chosenDistro);
    l.info("package system           : " + pm.getPackaging());
    l.info("default package map      : "
           + (defaultPackageMapURL == null ? "built-in"
                                          : defaultPackageMapURL.toString()));
    l.info("auxiliary package map    : "
           + (auxPackageMapURL == null ? "no" : auxPackageMapURL.toString()));
    l.info("type of project          : "
           + ((dc.getMainClass() != null) ? "application" : "library"));
    l.info("section                  : " + dc.getSection());
    l.info("bundle all dependencies  : " + ((dc.isBundleAll()) ? "yes" : "no"));
    l.info("ahead of time compilation: " + ((dc.isAotCompile()) ? "yes" : "no"));
    l.info("custom jar libraries     : "
            + ((dc.jarFiles.isEmpty()) ? "<none>"
                                      : String.valueOf(dc.jarFiles.size())));
    l.info("JNI libraries            : "
           + ((dc.jniFiles.isEmpty()) ? "<none>"
                                     : String.valueOf(dc.jniFiles.size())));
    l.info("auxiliary file source dir: "
           + (dc.srcAuxFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
                                             : dc.srcAuxFilesDir));
    l.info("auxiliary files          : "
           + ((dc.auxFiles.isEmpty()) ? "<none>"
                                     : String.valueOf(dc.auxFiles.size())));
    l.info("prefix                   : "
           + (dc.prefix.length() == 1 ? "/ (default)" : dc.prefix));
    l.info("sysconf files source dir : "
           + (dc.srcSysconfFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
                                                 : dc.srcSysconfFilesDir));
    l.info("sysconfdir               : "
           + (dc.sysconfdir.length() == 0 ? "(default)" : dc.sysconfdir));
    l.info("dataroot files source dir: "
           + (dc.srcDatarootFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
                                                  : dc.srcDatarootFilesDir));
    l.info("dataroot                 : "
           + (dc.datarootdir.length() == 0 ? "(default)" : dc.datarootdir));
    l.info("data files source dir    : "
           + (dc.srcDataFilesDir.length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
                                              : dc.srcDataFilesDir));
    l.info("datadir                  : "
           + (dc.datadir.length() == 0 ? "(default)" : dc.datadir));
    l.info("bindir                   : "
           + (dc.bindir.length() == 0 ? "(default)" : dc.bindir));

    if (dc.chosenDistro == null)
      throw new MojoExecutionException("No distribution configured!");

    if (dc.isAotCompile())
      {
        l.info("aot compiler             : " + dc.getGcjExec());
        l.info("aot classmap generator   : " + dc.getGcjDbToolExec());
      }

    if (dc.getMainClass() == null)
      {
        if (! "libs".equals(dc.getSection()))
          throw new MojoExecutionException(
                                           "section has to be 'libs' if no main class is given.");

        if (dc.isBundleAll())
          throw new MojoExecutionException(
                                           "Bundling dependencies to a library makes no sense.");
      }
    else
      {
        if ("libs".equals(dc.getSection()))
          throw new MojoExecutionException(
                                           "Set a proper section if main class parameter is set.");
      }

    if (dc.isAotCompile())
      {
        AotCompileUtils.setGcjExecutable(dc.getGcjExec());
        AotCompileUtils.setGcjDbToolExecutable(dc.getGcjDbToolExec());

        AotCompileUtils.checkToolAvailability();
      }
  }

  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    // Maven < 3.0.3 does not accept comma separated values as String[] so we need to split the values ourselves    
    String[] targetArray =  (target != null) ? target.split(",") : new String[]{defaultTarget};	
	
	for(String t : targetArray){
    
		// Generate merged distro configuration.		
	    String d = (distro != null) ? distro : getDefaultDistroForConfiguration(t);	    
		dc = getMergedConfiguration(t, d, true);
		dc.setChosenTarget(t);
	    
	    // Retrieve package map for chosen distro.
	    pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
	                        dc.bundleDependencies);

	    String packaging = pm.getPackaging();
	    
	    if (packaging == null){
	      throw new MojoExecutionException(
	                                       "Package maps document set no packaging for distro: "
	                                           + dc.chosenDistro);
	    }
	
	    // Create packager and packaging helper according to the chosen packaging type.	      
	    Helper ph = getPackagingHelperForPackaging(packaging, dc, pm);
	    Packager packager = getPackagerForPackaging(packaging);
	    
	    if (packager == null){
	      throw new MojoExecutionException("Unsupported packaging type: "+ packaging);
	    }
	  
	    // Store configuration in plugin-context for later use by signer- and deploy-goal
	    getPluginContext().put("dc", dc);
	    getPluginContext().put("pm", pm);
	    getPluginContext().put("packageVersion", ph.getPackageVersion());
	    
	    checkEnvironment(getLog());	
	    packager.checkEnvironment(getLog(), ph);
	    
	    packager.execute(getLog(), ph , pm);
	}
  }
  
  /**
   * Returns a Packager Object for a certain packaging type (deb, rpm, etc.) 
   * @param packaging
   * @return
   */
  private Packager getPackagerForPackaging(String packaging) {
	    Map<String, Packager> extPackagerMap = new HashMap<String,Packager>();
	    extPackagerMap.put("deb", new DebPackager());
	    extPackagerMap.put("ipk", new IpkPackager());
	    extPackagerMap.put("izpack", new IzPackPackager());
	    extPackagerMap.put("rpm", new RPMPackager());
	    return extPackagerMap.get(packaging);
  }

  /**
   * Returns a PackagingHelper object that supports a caertain packaging type
   * @param packaging
   * @param dc
   * @param pm
   * @return
   */
  public Helper getPackagingHelperForPackaging(String packaging, TargetConfiguration dc, PackageMap pm){
  		Map<String, Helper> extPackagerHelperMap = new HashMap<String,Helper>();
	    extPackagerHelperMap.put("deb", new DebHelper(dc, this));
	    extPackagerHelperMap.put("ipk", new IpkHelper(dc, this));
	    extPackagerHelperMap.put("izpack", new IzPackHelper(dc, this));
	    extPackagerHelperMap.put("rpm", new RpmHelper(dc, this));
	    return extPackagerHelperMap.get(packaging);
  }
  
  /**
   * Takes the default configuration and the custom one into account and creates
   * a merged one.
   * 
   * @param target Chosen target configuration
   * @param distro Chosen distro
   * @param mustMatch Whether a result must be found or whether it is OK to rely on defaults.
   * @return
   */
  private TargetConfiguration getMergedConfiguration(String target, String distro, boolean mustMatch)
      throws MojoExecutionException
  {
    Iterator<TargetConfiguration> ite = targetConfigurations.iterator();
    while (ite.hasNext())
      {
        TargetConfiguration currentTargetConfiguration = ite.next();
        
        // The target configuration should be for the requested target.
        if (!currentTargetConfiguration.target.equals(target))
        	continue;
        
        TargetConfiguration merged = getMergedConfiguration(currentTargetConfiguration.parent, distro, false);

        // Checks whether this targetconfiguration supports
        // the wanted distro.
        if (currentTargetConfiguration.distros.contains(distro) || merged.distros.contains(distro))
          {
            // Stores the chosen distro in the configuration for later use.
            currentTargetConfiguration.chosenDistro = distro;

            // Returns a configuration that is merged with
            // the default configuration-
            return currentTargetConfiguration.merge(merged);
          }
      }
    
    // For the target the user requested a result must be found (first case) but when the
    // plugin looks up parent configuration it will finally reach the default configuration
    // and for this it is necessary to derive from it without a match.
    if (mustMatch)
    {
    	throw new MojoExecutionException("Requested target " + target + " does not exist. Check spelling or configuration.");
    }
    else
    {
    	return new TargetConfiguration().merge(defaults);
    }
    
  }

  /**
   * Returns the default Distro to use for a certain TargetConfiguration.
   * @param target
   * @return
   * @throws MojoExecutionException 
   */
  private String getDefaultDistroForConfiguration(String target) throws MojoExecutionException{
	  String distro = new String();
	  Log l = getLog();
	    Iterator<TargetConfiguration> ite = targetConfigurations.iterator();
	    while (ite.hasNext())
	      {
	        TargetConfiguration currentTargetConfiguration = ite.next();
	        
	        // The target configuration should be for the requested target.
	        if (!currentTargetConfiguration.target.equals(target))
	        	continue;

	        // Checks whether this targetconfiguration supports
	        // the wanted distro.
	        if(currentTargetConfiguration.defaultDistro!=null){
	        	distro = currentTargetConfiguration.defaultDistro;
	        	l.info("Default distribution is set to \"" +distro + "\".");
	        }else if(currentTargetConfiguration.distros!=null){
			    if(currentTargetConfiguration.distros.size()==1){
			    	distro = (String) currentTargetConfiguration.distros.iterator().next();		
		        	l.info("Size of \"Distros\" list is one. Using \""+distro +"\" as default." );	    	
			    }else if(currentTargetConfiguration.distros.size()>1){
			    	String m = "No default configuration given for"+ currentTargetConfiguration.getTarget() 
			    	+", and more than one distro is supported. Please provide one.";
			    	l.error(m);
			    	throw new MojoExecutionException(m);
			    }
		    }else{
		    	throw new MojoExecutionException("No distros defined for configuration " + target);
		    }
	    	break;
	      }
	    return distro;	  
  }

public static String getDefaultSrcAuxfilesdir() {
	return DEFAULT_SRC_AUXFILESDIR;
}

	
  
  /**
   * Creates the temporary and package base directory.
   * 
   * @param l
   * @param basePkgDir
   * @throws MojoExecutionException
   */

}
