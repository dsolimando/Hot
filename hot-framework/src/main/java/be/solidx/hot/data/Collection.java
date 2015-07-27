package be.solidx.hot.data;

import java.util.Map;

public interface Collection<T extends Map<?,?>> {

	T findOne (T t);
	
	Cursor<T> find (T t);
	
	Cursor<T> find ();
	
	long count (T where);
	
	T update ( T where, T values);
	
	Collection<T> remove (T t);
	
	T insert (T t);
	
	Collection<T> drop ();
}
