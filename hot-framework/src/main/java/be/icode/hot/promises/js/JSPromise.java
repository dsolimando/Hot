package be.icode.hot.promises.js;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.FailPipe;
import org.jdeferred.ProgressCallback;
import org.jdeferred.ProgressPipe;
import org.jdeferred.Promise.State;
import org.jdeferred.impl.DeferredObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.promises.AbstractPromise;
import be.icode.hot.promises.Promise;

public class JSPromise extends AbstractPromise<NativeFunction> implements Promise<NativeFunction> {

	final Scriptable globalScope;
	
	@SuppressWarnings("rawtypes")
	public JSPromise(org.jdeferred.Promise promise, Scriptable globalscope) {
		super(promise);
		this.globalScope = globalscope;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public Promise<NativeFunction> then(final NativeFunction donePipe) {
		return new JSPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().resolve(value);
				}
			}
		}),globalScope);
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public Promise<NativeFunction> then(final NativeFunction donePipe, final NativeFunction failPipe) {
		return new JSPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().resolve(value);
				}
			}
		},new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = failPipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = failPipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().reject(value);
				}
			}
		}
		),globalScope);
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public Promise<NativeFunction> then(final NativeFunction donePipe, final NativeFunction failPipe, final NativeFunction progressPipe) {
		return new JSPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().resolve(value);
				}
			}
		}, new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = failPipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = failPipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().reject(value);
				}
			}
		},new ProgressPipe() {
			@Override
			public org.jdeferred.Promise pipeProgress(Object result) {
				Context context = Context.enter();
				Object value;
				if (result instanceof Object[]) {
					value = progressPipe.call(context, globalScope, donePipe, (Object[]) result);
				} else {
					value = progressPipe.call(context, globalScope, donePipe, new Object[]{result});
				}
				if (value instanceof NativeJavaObject)
					value = ((NativeJavaObject) value).unwrap();
				Context.exit();
				if (value instanceof Promise) {
					return ((Promise) value).getPromise();
				} else {
					return new DeferredObject<>().notify(value);
				}
			}
		}),globalScope);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<NativeFunction> done(final NativeFunction doneClosure) {
		promise.done(new DoneCallback() {
			@Override
			public void onDone(Object result) {
				Context context = Context.enter();
				if (result instanceof Object[]) {
					doneClosure.call(context, globalScope, doneClosure, (Object[]) result);
				} else {
					doneClosure.call(context, globalScope, doneClosure, new Object[]{result});
				}
				Context.exit();
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<NativeFunction> fail(final NativeFunction failClosure) {
		promise.fail(new FailCallback() {
			@Override
			public void onFail(Object result) {
				Context context = Context.enter();
				if (result instanceof Object[]) {
					failClosure.call(context, globalScope, failClosure, (Object[]) result);
				} else {
					failClosure.call(context, globalScope, failClosure, new Object[]{result});
				}
				Context.exit();
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<NativeFunction> progress(final NativeFunction progressClosure) {
		promise.progress(new ProgressCallback() {
			@Override
			public void onProgress(Object progress) {
				Context context = Context.enter();
				if (progress instanceof Object[]) {
					progressClosure.call(context, globalScope, progressClosure, (Object[]) progress);
				} else {
					progressClosure.call(context, globalScope, progressClosure, new Object[]{progress});
				}
				Context.exit();
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<NativeFunction> always(final NativeFunction alwaysClosure) {
		promise.always(new AlwaysCallback() {
			@Override
			public void onAlways(State state, Object resolved, Object rejected) {
				Context context = Context.enter();
				alwaysClosure.call(context, globalScope, alwaysClosure, new Object[]{});
				Context.exit();
			}
		});
		return this;
	}

	@Override
	public String state() {
		return promise.state().name();
	}
}
