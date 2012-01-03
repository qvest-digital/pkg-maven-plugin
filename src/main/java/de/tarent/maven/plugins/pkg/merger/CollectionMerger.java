package de.tarent.maven.plugins.pkg.merger;

import java.util.ArrayList;
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
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	 public <T> Object merge(Object child, Object parent, Object def) throws InstantiationException, IllegalAccessException {
						
			def = (def==null)? new Object(): def;
			
			Collection<T> c= (Collection<T>) def.getClass().newInstance();
			
			if (parent != null){
				c.addAll((Collection<T>)parent);
			}else{
				c.addAll((Collection<T>)def);
			}

			if (child != null){
				c.addAll((Collection<T>)child);
			}
			return (Collection<T>)c;
		}
}
