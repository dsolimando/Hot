package be.solidx.hot.promises.python;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.FailPipe;
import org.jdeferred.ProgressCallback;
import org.jdeferred.ProgressPipe;
import org.jdeferred.Promise.State;
import org.jdeferred.impl.DeferredObject;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import be.solidx.hot.promises.AbstractPromise;
import be.solidx.hot.promises.Promise;

public class PythonPromise extends AbstractPromise<PyFunction> implements Promise<PyFunction> {

	@SuppressWarnings("rawtypes")
	public PythonPromise(org.jdeferred.Promise promise) {
		super(promise);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> then(final PyFunction donePipeClosure) {
		DonePipe p = new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, donePipeClosure),Object.class);
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().resolve(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		};
		return new PythonPromise(promise.then(p));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> then(final PyFunction donePipeClosure, final PyFunction failPipeClosure) {
		return new PythonPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, donePipeClosure), Object.class);
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().resolve(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}, new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, failPipeClosure),Object.class);
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().reject(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> then(final PyFunction donePipeClosure, final PyFunction failPipeClosure, final PyFunction progressPipeClosure) {
		return new PythonPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, donePipeClosure),Object.class);
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().resolve(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}, new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, failPipeClosure),Object.class);
					if (result instanceof AbstractPromise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().reject(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}, new ProgressPipe () {
			@Override
			public org.jdeferred.Promise pipeProgress(Object value) {
				try {
					Object result = Py.tojava(callClosure(value, progressPipeClosure),Object.class);
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().notify(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> done(final PyFunction closure) {
		promise.done(new DoneCallback() {
			@Override
			public void onDone(Object value) {
				callClosure(value, closure);
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> fail(final PyFunction closure) {
		promise.fail(new FailCallback() {
			@Override
			public void onFail(Object value) {
				callClosure(value, closure);
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<PyFunction> progress(final PyFunction closure) {
		promise.progress(new ProgressCallback() {
			@Override
			public void onProgress(Object value) {
				callClosure(value, closure);
			}
		});
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Promise<PyFunction> always(final PyFunction closure) {
		promise.always(new AlwaysCallback() {
			@Override
			public void onAlways(State state, Object arg1, Object arg2) {
				closure.__call__();
			}
		});
		return this;
	}

	@Override
	public String state() {
		return promise.state().name();
	}
	
	private PyObject callClosure (Object value, PyFunction closure) {
		try {
			if (value instanceof Object[]) {
				Object[] objectValue = (Object[]) value;
				PyObject[] pyObjects = new PyObject[objectValue.length];
				int i = 0;
				for (Object object : objectValue) {
					pyObjects[i++] = Py.java2py(object);
				}
				return closure.__call__(pyObjects);
			} else {
				return closure.__call__(Py.java2py(value));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
