package be.solidx.hot.promises.js;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.promises.AbstractPromise;
import be.solidx.hot.promises.Promise;

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
				try {
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().resolve(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
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
				try {
					if (donePipe == null)
						return new DeferredObject<>().resolve(result);
					
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().resolve(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
				}
			}
		},new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object result) {
				try {
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = failPipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = failPipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().reject(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
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
				try {
					if (donePipe == null)
						return new DeferredObject<>().resolve(result);
					
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = donePipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = donePipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().resolve(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
				}
			}
		}, new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object result) {
				try {
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = failPipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = failPipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().reject(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
				}
			}
		},new ProgressPipe() {
			@Override
			public org.jdeferred.Promise pipeProgress(Object result) {
				try {
					Context context = Context.enter();
					Object value;
					if (result instanceof Object[] && !(result instanceof NativeArray)) {
						value = progressPipe.call(context, globalScope, donePipe, (Object[]) result);
					} else {
						value = progressPipe.call(context, globalScope, donePipe, new Object[]{result});
					}
					if (value instanceof NativeJavaObject)
						value = ((NativeJavaObject) value).unwrap();
					
					if (value instanceof Promise) {
						return ((Promise) value).getPromise();
					} else {
						return new DeferredObject<>().notify(value);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				} finally {
					Context.exit();
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
				if (result instanceof Object[] && !(result instanceof NativeArray)) {
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
				if (result instanceof Object[] && !(result instanceof NativeArray)) {
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
