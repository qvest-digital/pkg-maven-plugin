package de.tarent.maven.plugins.pkg;

import junit.framework.TestCase;

public class PathTest extends TestCase {

	public void testCreateFromUnixPath()
	{
		String input;
		int i;
		Path p;
		String[] exp;
		
		// normal paths
		input = "foo:bar:baz";
		exp = new String[] { "foo", "bar", "baz" };

		i = 0;
		p = Path.createFromUnixPath(input);
		for (String e : p)
			assertEquals("createFromUnixPath", exp[i++], e);
		assertEquals("number of elements", 3, i);

		// paths with spaces in them
		input = " foo:bar :baz bah";
		exp = new String[] { " foo", "bar ", "baz bah" };

		i = 0;
		p = Path.createFromUnixPath(input);
		for (String e : p)
			assertEquals("createFromUnixPath", exp[i++], e);
		assertEquals("number of elements", 3, i);
	}
	
	public void testToUnixPath()
	{
		String input;
		String expected;
		
		input = "foo:bar:baz";
		expected = input;
		assertEquals("toUnixPath", expected, Path.createFromUnixPath(input).toUnixPath());

		input = "foo:bar:baz bah";
		expected = "foo:bar:baz\\ bah";
		assertEquals("toUnixPath", expected, Path.createFromUnixPath(input).toUnixPath());

		input = " foo:bar :baz bah";
		expected = "\\ foo:bar\\ :baz\\ bah";
		assertEquals("toUnixPath", expected, Path.createFromUnixPath(input).toUnixPath());
	}

	public void testToWindowPath()
	{
		String input;
		String expected;
		
		input = "foo:bar:baz";
		expected = "foo;bar;baz";
		assertEquals("toWindowsPath", expected, Path.createFromUnixPath(input).toWindowPath());

		input = "foo:bar:baz bah";
		expected = "foo;bar;\"baz bah\"";
		assertEquals("toWindowsPath", expected, Path.createFromUnixPath(input).toWindowPath());

		input = " foo:bar :baz bah";
		expected = "\" foo\";\"bar \";\"baz bah\"";
		assertEquals("toWindowsPath", expected, Path.createFromUnixPath(input).toWindowPath());
	}
	
	public void testToWindowPath2()
	{
		String[] input;
		String expected;
		
		input = new String[] { "c:\\windows\\system32" }; 
		expected = "c:\\windows\\system32";
		assertEquals("absolute paths", expected, createPath(input).toWindowPath());

		input = new String[] { "d:/foo/bar" }; 
		expected = "d:\\foo\\bar";
		assertEquals("absolute paths", expected, createPath(input).toWindowPath());
		
		input = new String[] { "c:/my files/ja va" }; 
		expected = "\"c:\\my files\\ja va\"";
		assertEquals("absolute paths", expected, createPath(input).toWindowPath());

		input = new String[] { "c:\\windows\\system32", "d:/foo/bar", "c:/my files/ja va" }; 
		expected = "c:\\windows\\system32;d:\\foo\\bar;\"c:\\my files\\ja va\"";
		assertEquals("absolute paths", expected, createPath(input).toWindowPath());
	}
	
	private Path createPath(String[] values)
	{
		Path p = new Path();
		for (String v : values)
			p.append(v);
		
		return p;
	}
}
