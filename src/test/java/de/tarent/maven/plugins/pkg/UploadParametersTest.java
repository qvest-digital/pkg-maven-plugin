package de.tarent.maven.plugins.pkg;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class UploadParametersTest extends TestCase {

	@Test
	public void testReplacePlaceholders() {
		UploadParameters up = new UploadParameters();
		up.setUsername("foo");
		up.setPassword("foo2");
		Assert.assertEquals(
				up.parseUrlPlaceholders("ftp://localhost/%USERNAME%"),
				"ftp://localhost/foo");
		Assert.assertEquals(
				up.parseUrlPlaceholders("ftp://localhost/%PASSWORD%"),
				"ftp://localhost/foo2");
	}

}
