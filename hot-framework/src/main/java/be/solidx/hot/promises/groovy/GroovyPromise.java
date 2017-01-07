package be.solidx.hot.promises.groovy;

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

import groovy.lang.Closure;
import groovy.lang.Tuple;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.FailPipe;
import org.jdeferred.ProgressCallback;
import org.jdeferred.ProgressPipe;
import org.jdeferred.Promise.State;
import org.jdeferred.impl.DeferredObject;

import be.solidx.hot.promises.AbstractPromise;
import be.solidx.hot.promises.Promise;


public class GroovyPromise extends AbstractPromise<Closure<?>> {

	@SuppressWarnings("rawtypes")
	public GroovyPromise(org.jdeferred.Promise promise) {
		super(promise);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<Closure<?>> then(final Closure<?> donePipeClosure) {
		return new GroovyPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				Object result;
				try {
					if (value instanceof Object[]) {
						result = donePipeClosure.call(new Tuple((Object[]) value));
					} else {
						result = donePipeClosure.call(value);
					}
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().resolve(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		}));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<Closure<?>> then(final Closure<?> donePipeClosure, final Closure<?> failPipeClosure) {
		return new GroovyPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				try {
					if (donePipeClosure == null)
						return new DeferredObject<>().resolve(value);
					
					Object result;
					if (value instanceof Object[]) {
						result = donePipeClosure.call(new Tuple((Object[]) value));
					} else {
						result = donePipeClosure.call(value);
					}
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().resolve(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		},
		new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object value) {
				try {
					Object result;
					if (value instanceof Object[]) {
						result = failPipeClosure.call(new Tuple((Object[]) value));
					} else {
						result = failPipeClosure.call(value);
					}
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
	public Promise<Closure<?>> then(final Closure<?> donePipeClosure, final Closure<?> failPipeClosure, final Closure<?> progressPipeClosure) {
		return new GroovyPromise(promise.then(new DonePipe() {
			@Override
			public org.jdeferred.Promise pipeDone(Object value) {
				if (donePipeClosure == null)
					return new DeferredObject<>().resolve(value);
				Object result;
				if (value instanceof Object[]) {
					result = donePipeClosure.call(new Tuple((Object[]) value));
				} else {
					result = donePipeClosure.call(value);
				}
				if (result instanceof Promise) {
					return ((Promise) result).getPromise();
				} else {
					return new DeferredObject<>().resolve(result);
				}
			}
		},
		new FailPipe() {
			@Override
			public org.jdeferred.Promise pipeFail(Object value) {
				try {
					Object result;
					if (value instanceof Object[]) {
						result = failPipeClosure.call(new Tuple((Object[]) value));
					} else {
						result = failPipeClosure.call(value);
					}
					if (result instanceof Promise) {
						return ((Promise) result).getPromise();
					} else {
						return new DeferredObject<>().reject(result);
					}
				} catch (Exception e) {
					return new DeferredObject<>().reject(e);
				}
			}
		},
		new ProgressPipe() {
			@Override
			public org.jdeferred.Promise pipeProgress(Object value) {
				try {
					Object result;
					if (value instanceof Object[]) {
						result = progressPipeClosure.call(new Tuple((Object[]) value));
					} else {
						result = progressPipeClosure.call(value);
					}
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
	public Promise<Closure<?>> done(final Closure<?> closure) {
		promise.done(new DoneCallback() {
			@Override
			public void onDone(final Object value) {
				if (value instanceof Object[]) {
					closure.call((Object[]) value);
				} else {
					closure.call(value);
				}
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<Closure<?>> fail(final Closure<?> closure) {
		promise.fail(new FailCallback () {
			@Override
			public void onFail(final Object value) {
				if (value instanceof Object[]) {
					closure.call((Object[]) value);
				} else {
					closure.call(value);
				}
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<Closure<?>> progress(final Closure<?> closure) {
		promise.progress(new ProgressCallback() {
			@Override
			public void onProgress(Object progressValue) {
				if (progressValue instanceof Object[]) {
					closure.call((Object[]) progressValue);
				} else {
					closure.call(progressValue);
				}
			}
		});
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Promise<Closure<?>> always(final Closure<?> closure) {
		promise.always(new AlwaysCallback() {
			@Override
			public void onAlways(State state, Object resultValue, Object rejectValue) {
				closure.call();
			}
		});
		return this;
	}

	@Override
	public String state() {
		return promise.state().name();
	}
}
