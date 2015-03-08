package be.solidx.hot.promises.js;

import org.jdeferred.impl.DeferredObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

public class JSDeferred extends JSPromise implements Deferred<NativeFunction> {

	public JSDeferred(Scriptable globalscope) {
		super(new DeferredObject<>(), globalscope);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSDeferred resolve(Object... objects) {
		((DeferredObject)promise).resolve(objects);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSDeferred reject(Object... objects) {
		((DeferredObject)promise).reject(objects);
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JSDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.notify(notificationValues);
		return this;
	}

	@Override
	public Promise<NativeFunction> promise() {
		return this;
	}

}
