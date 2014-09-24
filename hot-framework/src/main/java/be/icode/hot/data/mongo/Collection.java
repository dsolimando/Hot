package be.icode.hot.data.mongo;

import java.util.Map;

public interface Collection<T extends Map<?,?>> extends be.icode.hot.data.Collection<T> {
	
	T findOne ();
	
	T save (T t);
	
	T runCommand (String command, T t);
}
