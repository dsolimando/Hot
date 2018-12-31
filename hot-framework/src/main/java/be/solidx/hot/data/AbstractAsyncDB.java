package be.solidx.hot.data;

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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.ClosureExecutor;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAsyncDB<CLOSURE,T extends Map<?, ?>> implements AsyncDB<CLOSURE, T>, ClosureExecutor<CLOSURE> {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncDB.class);

	protected DB<T> db;
	
	ExecutorService blockingTasksThreadPool;
	
	ExecutorService eventLoop;
	
	public AbstractAsyncDB(DB<T> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		this.db = db;
		this.blockingTasksThreadPool = blockingTasksThreadPool;
		this.eventLoop = eventLoop;
	}

	@Override
	public AsyncCollection<CLOSURE, T> getCollection(String name) {
		return buildAsyncCollection(db.getCollection(name));
	}

	@Override
	public Promise<CLOSURE> listCollections(final CLOSURE successClosure, final CLOSURE failClosure) {
		return deferredBlockingCall(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return db.listCollections();
			}
		}, successClosure, failClosure);
	}
	
	@Override
	public Promise<CLOSURE> listCollections(final CLOSURE successClosure) {
		return listCollections(successClosure, null);
	}
	
	@Override
	public Promise<CLOSURE> listCollections() {
		return listCollections(null);
	}

	@Override
	public Promise<CLOSURE> getPrimaryKeys(final String collection, final CLOSURE successClosure, final CLOSURE failClosure) {
		return deferredBlockingCall(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return db.getPrimaryKeys(collection);
			}
		}, successClosure, failClosure);
	}
	
	@Override
	public Promise<CLOSURE> getPrimaryKeys(final String collection, final CLOSURE successClosure) {
		return getPrimaryKeys(collection, successClosure, null);
	}
	
	@Override
	public Promise<CLOSURE> getPrimaryKeys(String collection) {
		return getPrimaryKeys(collection, null);
	}
	
	protected abstract Deferred<CLOSURE> buildDeferred ();
	
	protected abstract AsyncCursor<CLOSURE, T> buildAsyncCursor(Cursor<T> cursor);
	
	protected abstract AsyncCollection<CLOSURE, T> buildAsyncCollection(Collection<T> collection);

	protected Promise<CLOSURE> deferredBlockingCall (final Callable<Object> callable, final CLOSURE successCallback, final CLOSURE failCallback) {
		
		final Deferred<CLOSURE> deferred = buildDeferred();
		
		blockingTasksThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					final Object result = callable.call();
					if (successCallback != null) {
						eventLoop.execute(new Runnable() {
							@Override
							public void run() {
								executeClosure(successCallback, result);
							}
						});
					}
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							deferred.resolve(result);
						}
					});
				} catch (final Exception e) {
				    if (LOGGER.isErrorEnabled()) {
				        LOGGER.error("",e);
                    }
					if (failCallback != null) {
						eventLoop.execute(new Runnable() {
							@Override
							public void run() {
								executeClosure(failCallback, e);
							}
						});
					}
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							deferred.reject(e);
						}
					});
				}
			}
		});
		return deferred.promise();
	}
	
	public abstract class AbstractAsyncCollection implements AsyncCollection<CLOSURE,T> {

		protected Collection<T> collection;
		
		public AbstractAsyncCollection(Collection<T> collection) {
			this.collection = collection;
		}

		@Override
		public Promise<CLOSURE> findOne(final T t, final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				
				@Override
				public Object call() throws Exception {
					return collection.findOne(t);
				}
			}, successCallback, failCallback);
		}
		
		@Override
		public Promise<CLOSURE> findOne(final T t, final CLOSURE sucessCallback) {
			return findOne(t, sucessCallback, null);
		}

		@Override
		public Promise<CLOSURE> findOne(final T t) {
			return findOne(t, null);
		}

		@Override
		public Promise<CLOSURE> count(final T where, final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return collection.count(where);
				}
			}, successCallback, failCallback);
		}
		
		@Override
		public Promise<CLOSURE> count(final T where) {
			return count(where, null);
		}

		@Override
		public Promise<CLOSURE> count(final T where, final CLOSURE successCallback) {
			return count(where, successCallback, null);
		}
		
		
		@Override
		public Promise<CLOSURE> update(final T where, final T values) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return collection.update(where, values);
				}
			}, null, null);
		}

		@Override
		public Promise<CLOSURE> remove(final T t, final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return collection.remove(t);
				}
			}, successCallback, failCallback);
		}
		
		@Override
		public Promise<CLOSURE> remove(final T t, final CLOSURE successCallback) {
			return remove(t, successCallback, null);
		}
		
		@Override
		public Promise<CLOSURE> remove(final T t) {
			return remove(t, null);
		}


		@Override
		public Promise<CLOSURE> insert(final T t) {
			return insert(t, null);
		}

		@Override
		public Promise<CLOSURE> insert(final T t, final CLOSURE successCallback) {
			return insert(t, successCallback, null);
		}
		
		@Override
		public Promise<CLOSURE> insert(final T t, final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return collection.insert(t);
				}
			}, successCallback, failCallback);
		}

		@Override
		public Promise<CLOSURE> drop() {
			return drop(null);
		}

		@Override
		public Promise<CLOSURE> drop(CLOSURE successCallback) {
			return drop(successCallback, null);
		}
		
		@Override
		public Promise<CLOSURE> drop(final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return collection.drop();
				}
			}, successCallback, failCallback);
		}
		
		@Override
		public AsyncCursor<CLOSURE, T> find() {
			return buildAsyncCursor(collection.find());
		}
		
		@Override
		public AsyncCursor<CLOSURE,T> find(T t) {
			return buildAsyncCursor(collection.find(t));
		};
	}
	
	public abstract class AbstractAsyncCursor implements AsyncCursor<CLOSURE,T> {
		
		protected Cursor<T> cursor;
		
		public AbstractAsyncCursor(Cursor<T> cursor) {
			this.cursor = cursor;
		}

		@Override
		public Promise<CLOSURE> count(final CLOSURE successCallback, final CLOSURE failCallback) {
			
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return cursor.count();
				}
			}, successCallback, failCallback);
		}
		
		@Override
		public Promise<CLOSURE> count(final CLOSURE closureSucceed) {
			return count(closureSucceed, null);
		}
		
		@Override
		public Promise<CLOSURE> count() {
			return count(null);
		}

		@Override
		public AsyncCursor<CLOSURE,T> limit(Integer limit) {
			cursor.limit(limit);
			return this;
		}

		@Override
		public AsyncCursor<CLOSURE,T> skip(Integer at) {
			cursor.skip(at);
			return this;
		}

		@Override
		public AsyncCursor<CLOSURE,T> sort(T sortMap) {
			cursor.sort(sortMap);
			return this;
		}
		
		@Override
		public Promise<CLOSURE> promise(CLOSURE successClosure) {
			return promise(successClosure, null);
		}

		@Override
		public Promise<CLOSURE> promise() {
			return promise(null);
		}
	}
}
