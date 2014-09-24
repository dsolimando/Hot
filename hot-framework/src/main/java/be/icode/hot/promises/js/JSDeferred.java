package be.icode.hot.promises.js;

import org.jdeferred.impl.DeferredObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.Promise;

public class JSDeferred extends JSPromise implements Deferred<NativeFunction> {

	public JSDeferred(Scriptable globalscope) {
		super(new DeferredObject<>(), globalscope);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void resolve(Object... objects) {
		((DeferredObject)promise).resolve(objects);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void reject(Object... objects) {
		((DeferredObject)promise).reject(objects);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.notify(notificationValues);
	}

	@Override
	public Promise<NativeFunction> promise() {
		return this;
	}

}
