package de.tarent.maven.plugins.pkg.merger;

import java.util.Properties;

/**
 * Merger for properties
 * @author plafue
 *
 */
public class PropertiesMerger implements IMerge {

	/**
	 * If child != null, take child (overridden parent), else if parent != null,
	 * take parent (overridden default), else take default.
	 * 
	 * @param child
	 * @param parent
	 * @param def
	 * @return
	 */
	public Properties merge(Object child, Object parent,
			Object def) {
		Properties c = ((Properties)parent != null ? (Properties)parent : (Properties)def);

		if (child != null){
			c.putAll((Properties)child);
		}
		return (Properties)c;
	}

}
