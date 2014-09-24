package be.icode.hot.data;

import java.util.Map;

import be.icode.hot.promises.Promise;

public interface AsyncDB<CLOSURE, T extends Map<?,?>> {

	AsyncCollection<CLOSURE, T> getCollection (String name);
	
	Promise<CLOSURE> listCollections();
	
	Promise<CLOSURE> listCollections(CLOSURE successClosure);
			
	Promise<CLOSURE> listCollections(CLOSURE successClosure, CLOSURE failClosure);
	
	Promise<CLOSURE> getPrimaryKeys(String collection);
	
	Promise<CLOSURE> getPrimaryKeys(String collection, CLOSURE successClosure);
	
	Promise<CLOSURE> getPrimaryKeys(String collection, CLOSURE successClosure, CLOSURE failClosure);
}
