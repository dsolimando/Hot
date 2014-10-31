package be.icode.hot.data;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import be.icode.hot.ClosureExecutor;
import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.Promise;

public abstract class AbstractAsyncDB<CLOSURE,T extends Map<?, ?>> implements AsyncDB<CLOSURE, T>, ClosureExecutor<CLOSURE> {

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
					// We are faking a thread creation from outside the eventLoop
					Thread.currentThread().setContextClassLoader(blockingTasksThreadPool.getClass().getClassLoader());
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
		public Promise<CLOSURE> update(final T values, final T where, final CLOSURE successCallback, final CLOSURE failCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return collection.update(values, where);
				}
			}, successCallback, failCallback);
		}

		@Override
		public Promise<CLOSURE> update(final T values, final T where, final CLOSURE successCallback) {
			return update(values, where, successCallback, null);
		}
		
		@Override
		public Promise<CLOSURE> update(final T values, final T where) {
			return update(values, where, null);
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
