package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Representation of a path, ie. something that is used for specifing a classpath, a boot classpath.
 * 
 * <p>It consists of entries. By using the methods {@link #toUnixPath()} and {@link #toWindowPath()}
 * the path can be translated into something which a particular platform can understand.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class Path implements Iterable<String> {
	
	private static final Pattern UNIX_PATH_PATTERN = Pattern.compile(":");
	
	private static final Pattern UNIX_SPACE_PATTERN = Pattern.compile(" ");

	private static final Pattern WINDOWS_SLASH_PATTERN = Pattern.compile("/");
	
	private LinkedList<String> pathEntries = new LinkedList<String>();
	
	/**
	 * Creates an empty path.
	 */
	public Path()
	{
		// Intentionally does nothing.
	}
	
	/**
	 * Creates a path with a single entry.
	 * 
	 * @param e
	 */
	public Path(File dir)
	{
		append(dir.toString());
	}
	
	public static Path createFromUnixPath(String unixPath)
	{
		Path p = new Path();
		String[] entries = UNIX_PATH_PATTERN.split(unixPath);
		for (String e : entries)
			p.pathEntries.add(e);
		
		return p;
	}

	public Iterator<String> iterator()
	{
		return pathEntries.iterator();
	}
	
	public void append(String e)
	{
		pathEntries.addLast(e);
	}
	
	public void prepend(String e)
	{
		pathEntries.addFirst(e);
	}
	
	public String toUnixPath()
	{
		StringBuffer sb = new StringBuffer(pathEntries.size() * 10);
		
		Iterator<String> ite = pathEntries.iterator();
		if (ite.hasNext()){
			sb.append(fixForUnix(ite.next()));
		}
		while (ite.hasNext())
		{
			sb.append(":");
			sb.append(fixForUnix(ite.next()));
		}
		
		return sb.toString();
	}
	
	private static String fixForUnix(String entry)
	{
		// Quote entries with spaces in them.
		String e = UNIX_SPACE_PATTERN
			.matcher(entry)
			.replaceAll("\\\\ ");
		
		return e;
	}
	
	public String toWindowPath()
	{
		StringBuffer sb = new StringBuffer(pathEntries.size() * 10);
		
		Iterator<String> ite = pathEntries.iterator();
		if (ite.hasNext()){
			sb.append(fixForWindows(ite.next()));
		}
		while (ite.hasNext())
		{
			sb.append(";");
			
			sb.append(fixForWindows(ite.next()));
		}
		
		return sb.toString();
	}
	
	private static String fixForWindows(String entry)
	{
		// Use backslashes in path entry.
		String e = WINDOWS_SLASH_PATTERN
			.matcher(entry)
			.replaceAll("\\\\");
		
		// Quote entries with spaces in them.
		if (e.contains(" ")){
			e = "\"" + e + "\"";
		}
		return e;
	}

}
