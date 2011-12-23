package de.tarent.maven.plugins.pkg.merger;

/**
 * Interface for Merger objects.
 * @author plafue
 *
 */
public interface IMerge {
	<T> Object merge(Object child, Object parent, Object def);
}
