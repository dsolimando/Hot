package be.solidx.hot.data.mongo;

import java.util.Map;

public interface Collection<T extends Map<?,?>> extends be.solidx.hot.data.Collection<T> {
	
	T findOne ();
	
	T update (T where, T values , boolean upsert, boolean multi);
	
	T save (T t);
	
	T runCommand (String command, T t);
}
