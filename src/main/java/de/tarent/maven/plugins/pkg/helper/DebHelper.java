package de.tarent.maven.plugins.pkg.helper;

import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
import de.tarent.maven.plugins.pkg.TargetConfiguration;

public class DebHelper extends Helper{

	public DebHelper(TargetConfiguration targetConfiguration, AbstractPackagingMojo mojo) {
		super(targetConfiguration, mojo);
	}

}
