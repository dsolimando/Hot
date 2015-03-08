package be.solidx.hot.promises.groovy;

import groovy.lang.Closure;

import org.jdeferred.impl.DeferredObject;

import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

public class GroovyDeferred extends GroovyPromise implements Deferred<Closure<?>> {

	public GroovyDeferred() {
		super(new DeferredObject<>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public GroovyDeferred resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (resolveValues.length == 1) {
			deferredObject.resolve(resolveValues[0]);
		} else {
			deferredObject.resolve(resolveValues);
		}
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public GroovyDeferred reject(Object... rejectValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (rejectValues.length == 1) {
			deferredObject.reject(rejectValues[0]);
		} else {
			deferredObject.reject(rejectValues);
		}
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public GroovyDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (notificationValues.length == 1) {
			deferredObject.notify(notificationValues[0]);
		} else {
			deferredObject.notify(notificationValues);
		}
		return this;
	}

	@Override
	public Promise<Closure<?>> promise() {
		return this;
	}
}
