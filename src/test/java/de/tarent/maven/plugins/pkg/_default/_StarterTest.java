package de.tarent.maven.plugins.pkg._default;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

public class _StarterTest {

	@Test
	public void testStarter_parse() throws Exception {
		// _Starter lies in the default package and thus needs to be accessed in a quirky way
		Class<?> c = Class.forName("_Starter");
		Method m = c.getDeclaredMethod("parse", new Class[] { String.class, LinkedList.class });
		m.setAccessible(true);
		
		LinkedList<URL> urls = new LinkedList<URL>();
		String className = (String) m.invoke(null, new Object[] { "_classpath", urls } );
		
		// See the class name line in the _classpath resource.
		Assert.assertEquals("de.tarent.maven.plugins.pkg._default.StartMe", className);
		Assert.assertEquals(3, urls.size());
		Assert.assertEquals(toURL("foo.jar"), urls.get(0)); 
		Assert.assertEquals(toURL("/somewhere/bar.jar"), urls.get(1)); 
		Assert.assertEquals(toURL("../baz.jar"), urls.get(2)); 
	}
	
	private URL toURL(String f) throws Exception {
		return new File(f).toURI().toURL();
	}

	@Test
	public void testStarter_main() throws Exception {
		// _Starter lies in the default package and thus needs to be accessed in a quirky way
		Class<?> c = Class.forName("_Starter");
		Method m = c.getMethod("main", new Class[] { String[].class });
		
		String[] args = new String[] { "foo", "bar", "baz" };

		m.invoke(null, new Object[] { args } );
		
		Assert.assertEquals(args, StartMe.last_args);
	}
	
}
