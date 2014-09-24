package be.icode.hot.promises.groovy;

import groovy.lang.Closure;

import org.jdeferred.impl.DeferredObject;

import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.Promise;

public class GroovyDeferred extends GroovyPromise implements Deferred<Closure<?>> {

	public GroovyDeferred() {
		super(new DeferredObject<>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (resolveValues.length == 1) {
			deferredObject.resolve(resolveValues[0]);
		} else {
			deferredObject.resolve(resolveValues);
		}
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void reject(Object... rejectValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (rejectValues.length == 1) {
			deferredObject.reject(rejectValues[0]);
		} else {
			deferredObject.reject(rejectValues);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (notificationValues.length == 1) {
			deferredObject.notify(notificationValues[0]);
		} else {
			deferredObject.notify(notificationValues);
		}
	}

	@Override
	public Promise<Closure<?>> promise() {
		return this;
	}
}
