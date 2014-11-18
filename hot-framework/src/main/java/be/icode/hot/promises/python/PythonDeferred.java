package be.icode.hot.promises.python;

import org.jdeferred.impl.DeferredObject;
import org.python.core.PyFunction;

import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.Promise;

public class PythonDeferred extends PythonPromise implements Deferred<PyFunction> {

	public PythonDeferred() {
		super(new DeferredObject<>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public PythonDeferred resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.resolve(resolveValues);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public PythonDeferred reject(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(resolveValues);
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public PythonDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(notificationValues);
		return this;
	}

	@Override
	public Promise<PyFunction> promise() {
		return this;
	}

}
