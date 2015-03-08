package be.solidx.hot.promises;

public interface Promise<CLOSURE> {

	Promise<CLOSURE> then(CLOSURE doneClosure);
	Promise<CLOSURE> then(CLOSURE doneClosure, CLOSURE failClosure);
	Promise<CLOSURE> then(CLOSURE doneClosure, CLOSURE failClosure, CLOSURE progressClosure);
	
	Promise<CLOSURE> done(CLOSURE closure);
	Promise<CLOSURE> _done(DCallback callback);
	
	Promise<CLOSURE> fail(CLOSURE closure);
	Promise<CLOSURE> _fail(FCallback callback);
	
	Promise<CLOSURE> progress(CLOSURE closure);
	Promise<CLOSURE> always (CLOSURE closure);
	
	String state();
	
	@SuppressWarnings("rawtypes")
	org.jdeferred.Promise getPromise();
	
	public interface DCallback {
		void onDone (Object result);
	}
	public interface FCallback {
		void onFail (Object throwable);
	}
}
