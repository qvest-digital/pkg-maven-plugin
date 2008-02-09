package de.tarent.maven.plugins.pkg;

/**
 * This class is used by Maven2 to fill the datarootfiles entries.
 * 
 * <p>It exists just to please Maven2 automatic type mapping. Technically
 * we are only interested that this type is compatible to <code>AuxFile</code>
 * to let common utility methods work on it nicely.</p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
public class DatarootFile
    extends AuxFile
{
}
