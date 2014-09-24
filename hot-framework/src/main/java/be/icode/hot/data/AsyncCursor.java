package be.icode.hot.data;

import java.util.Map;

import be.icode.hot.promises.Promise;

public interface AsyncCursor<CLOSURE, T extends Map<?, ?>> {
	
	Promise<CLOSURE> promise();
	
	Promise<CLOSURE> promise(CLOSURE successClosure);
	
	Promise<CLOSURE> promise(CLOSURE successClosure, CLOSURE failClosure);
	
	Promise<CLOSURE> count(CLOSURE successClosure, CLOSURE failClosure);
	
	Promise<CLOSURE> count(CLOSURE successClosure);
	
	Promise<CLOSURE> count();
	
	AsyncCursor<CLOSURE,T> limit(Integer limit);
	
	AsyncCursor<CLOSURE,T> skip(Integer at);
	
	AsyncCursor<CLOSURE,T> sort(T sortMap);
}
