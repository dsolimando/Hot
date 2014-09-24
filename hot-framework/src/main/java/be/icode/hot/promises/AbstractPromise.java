package be.icode.hot.promises;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;


public abstract class AbstractPromise<CLOSURE> implements Promise<CLOSURE> {

	@SuppressWarnings("rawtypes")
	protected org.jdeferred.Promise promise;
	
	@SuppressWarnings("rawtypes")
	public AbstractPromise(org.jdeferred.Promise promise) {
		this.promise = promise;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Promise<CLOSURE> _done(final DCallback callback) {
		promise.done(new DoneCallback<Object>() {
			@Override
			public void onDone(Object result) {
				callback.onDone(result);
			}
		});
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Promise<CLOSURE> _fail(final FCallback callback) {
		promise.fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable throwable) {
				callback.onFail(throwable);
			}
		});
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	public org.jdeferred.Promise getPromise() {
		return promise;
	}
}
