package be.solidx.hot.data;

import java.util.Map;

import be.solidx.hot.promises.Promise;

public interface AsyncCollection<CLOSURE, T extends Map<?,?>> {

	Promise<CLOSURE> findOne (T t);

	Promise<CLOSURE> findOne (T t, CLOSURE sucessCallback);

	Promise<CLOSURE> findOne (T t, CLOSURE sucessCallback, CLOSURE failCallback);
	
	AsyncCursor<CLOSURE,T> find (T t);
	
	AsyncCursor<CLOSURE,T> find ();
	
	Promise<CLOSURE> count (T where);
	
	Promise<CLOSURE> count (T where, CLOSURE successCallback);

	Promise<CLOSURE> count (T where, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> update (T values, T where);
	
	Promise<CLOSURE> update (T values, T where, CLOSURE successCallback);
	
	Promise<CLOSURE> update (T values, T where, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> remove (T t);
	
	Promise<CLOSURE> remove (T t, CLOSURE successCallback);
	
	Promise<CLOSURE> remove (T t, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> insert (T t);
	
	Promise<CLOSURE> insert (T t, CLOSURE successCallback);
	
	Promise<CLOSURE> insert (T t, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> drop ();
	
	Promise<CLOSURE> drop (CLOSURE successCallback);
	
	Promise<CLOSURE> drop (CLOSURE successCallback, CLOSURE failCallback);
}
