package de.tarent.maven.plugins.pkg.merger;

import java.util.Collection;

/**
 * Merger for collections
 * @author plafue
 *
 */
public class CollectionMerger implements IMerge {

	/**
	 * If child != null, take child (overridden parent), else if parent != null,
	 * take parent (overridden default), else take default.
	 * @param <T>
	 * @param <T>
	 * 
	 * @param child
	 * @param parent
	 * @param def
	 * @return
	 */
	 public <T> Object merge(Object child, Object parent, Object def) {
						
			def = (def==null)? new Object(): def;
			
			Collection<T> c = (parent != null ? (Collection<T>)parent : (Collection<T>)def);

			if (child != null){
				c.addAll((Collection<T>)child);
			}
			return (Collection<T>)c;
		}
}
