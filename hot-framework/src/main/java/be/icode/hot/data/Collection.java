package be.icode.hot.data;

import java.util.Map;

public interface Collection<T extends Map<?,?>> {

	T findOne (T t);
	
	Cursor<T> find (T t);
	
	Cursor<T> find ();
	
	long count (T where);
	
	T update (T values, T where);
	
	Collection<T> remove (T t);
	
	T insert (T t);
	
	Collection<T> drop ();
}
