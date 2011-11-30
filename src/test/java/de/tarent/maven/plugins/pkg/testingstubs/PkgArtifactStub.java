package de.tarent.maven.plugins.pkg.testingstubs;

import java.io.File;

import org.apache.maven.plugin.testing.stubs.ArtifactStub;
/**
 * Defines a stub artifact for testing purposes 
 * @author plafue
 *
 */
public class PkgArtifactStub

extends ArtifactStub{

	public PkgArtifactStub(String path){
		super.setFile(new File(path));
	}

	public PkgArtifactStub(File file){
		super.setFile(file);
	}

}