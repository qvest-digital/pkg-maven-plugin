package de.tarent.maven.plugins.pkg.helper;

import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
import de.tarent.maven.plugins.pkg.TargetConfiguration;
import de.tarent.maven.plugins.pkg.map.PackageMap;

public class IpkHelper extends Helper {

	public IpkHelper(AbstractPackagingMojo mojo, PackageMap packageMap, TargetConfiguration tc) {
		super(mojo, packageMap, tc);
	}


}
