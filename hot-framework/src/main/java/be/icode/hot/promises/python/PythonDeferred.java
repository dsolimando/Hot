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
	public void resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.resolve(resolveValues);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void reject(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(resolveValues);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(notificationValues);
	}

	@Override
	public Promise<PyFunction> promise() {
		return this;
	}

}
