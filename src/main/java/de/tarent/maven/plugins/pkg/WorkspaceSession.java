package de.tarent.maven.plugins.pkg;

import java.util.List;

import de.tarent.maven.plugins.pkg.helper.Helper;
import de.tarent.maven.plugins.pkg.map.PackageMap;
import de.tarent.maven.plugins.pkg.packager.Packager;

/**
 * <p>
 * In order for the plugin to process a <em>single</em>
 * {@link TargetConfiguration} a number of runtime data is needed by several
 * actors. Instances of this class can be used to store and retrieve all what is
 * needed. Those objects are considered central. However users should not get
 * too greedy and store a {@link WorkspaceSession} object everywhere. Instead
 * they should only be provided the objects to work on and high level objects
 * should hand out the {@link WorkspaceSession} instance.
 * </p>
 * 
 * <p>This class is supposed to contain mid level work objects. Do not put low
 * level stuff like a path or individual string properties here. Conversely if
 * you somewhere get a work object instance from anything else than either a
 * method's arguments or a {@link WorkspaceSession} instance it is breaking the
 * </p>
 * 
 */
public class WorkspaceSession {

	AbstractPackagingMojo mojo;

	TargetConfiguration targetConfiguration;

	List<TargetConfiguration> buildChain;

	PackageMap packageMap;
	
	Helper helper;
	
	Packager packager;

	public AbstractPackagingMojo getMojo() {
		return mojo;
	}

	public void setMojo(AbstractPackagingMojo mojo) {
		this.mojo = mojo;
	}
	
	public TargetConfiguration getTargetConfiguration() {
		return targetConfiguration;
	}

	public void setTargetConfiguration(TargetConfiguration targetConfiguration) {
		this.targetConfiguration = targetConfiguration;
	}

	public List<TargetConfiguration> getBuildChain() {
		return buildChain;
	}

	public void setBuildChain(List<TargetConfiguration> buildChain) {
		this.buildChain = buildChain;
	}

	public PackageMap getPackageMap() {
		return packageMap;
	}

	public void setPackageMap(PackageMap packageMap) {
		this.packageMap = packageMap;
	}

	public Helper getHelper() {
		return helper;
	}

	public void setHelper(Helper helper) {
		this.helper = helper;
	}

	public Packager getPackager() {
		return packager;
	}

	public void setPackager(Packager packager) {
		this.packager = packager;
	}

}
