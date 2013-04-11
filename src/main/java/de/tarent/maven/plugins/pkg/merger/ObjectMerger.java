package de.tarent.maven.plugins.pkg.merger;

/**
 * Merger for all Objects
 * 
 * @author plafue
 * 
 */
public class ObjectMerger implements IMerge {

	/**
	 * If child != null, take child (overridden parent), else if parent != null,
	 * take parent (overridden default), else take default.
	 * 
	 * @param child
	 * @param parent
	 * @param def
	 * @return
	 */
	public Object merge(Object child, Object parent, Object def) {
		return (child != null) ? child : (parent != null ? parent : def);
	}
}
