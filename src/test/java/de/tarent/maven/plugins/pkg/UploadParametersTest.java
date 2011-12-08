package de.tarent.maven.plugins.pkg;

import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UploadParametersTest extends TestCase{

	@Test
	public void testReplacePlaceholders(){
		UploadParameters up = new UploadParameters();
		up.setUsername("foo");
		up.setPassword("foo2");
		Assert.assertEquals(up.parseUrlPlaceholders("ftp://localhost/%USERNAME%"),"ftp://localhost/foo");
		Assert.assertEquals(up.parseUrlPlaceholders("ftp://localhost/%PASSWORD%"),"ftp://localhost/foo2");
		
	}
}
