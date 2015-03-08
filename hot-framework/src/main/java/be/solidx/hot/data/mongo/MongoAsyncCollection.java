package be.solidx.hot.data.mongo;

import java.util.Map;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.promises.Promise;

public interface MongoAsyncCollection<CLOSURE,T extends Map<?, ?>> extends AsyncCollection<CLOSURE, T> {
	
	Promise<CLOSURE> save (T t);
	Promise<CLOSURE> save (T t,CLOSURE successCallback);
	Promise<CLOSURE> save (T t,CLOSURE successCallback, CLOSURE errorCallback);
	
	Promise<CLOSURE> runCommand (String command, T t);
	Promise<CLOSURE> runCommand (String command, T t, CLOSURE successCallback);
	Promise<CLOSURE> runCommand (String command, T t, CLOSURE successCallback, CLOSURE errorCallback);
}
