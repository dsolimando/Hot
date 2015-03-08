package be.solidx.hot.data.scripting;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.data.AbstractAsyncDB;
import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.AsyncCursor;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.Cursor;
import be.solidx.hot.data.DB;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.groovy.GroovyDeferred;

public class GroovyAsyncDB extends AbstractAsyncDB<Closure<?>, Map<String, Object>> {

	public GroovyAsyncDB(DB<Map<String, Object>> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	public Object executeClosure(Closure<?> closure, Object... args) {
		return closure.call(args);
	}

	@Override
	protected Deferred<Closure<?>> buildDeferred() {
		return new GroovyDeferred();
	}

	@Override
	protected AsyncCollection<Closure<?>, Map<String, Object>> buildAsyncCollection(Collection<Map<String, Object>> collection) {
		return new GroovyAsyncCollection(collection);
	}
	
	@Override
	protected AsyncCursor<Closure<?>, Map<String, Object>> buildAsyncCursor(Cursor<Map<String, Object>> cursor) {
		return new GroovyAsyncCursor(cursor);
	}

	protected class GroovyAsyncCollection extends AbstractAsyncCollection {

		public GroovyAsyncCollection(Collection<Map<String, Object>> collection) {
			super(collection);
		}
	}
	
	private class GroovyAsyncCursor extends AbstractAsyncCursor {

		public GroovyAsyncCursor(Cursor<Map<String, Object>> cursor) {
			super(cursor);
		}

		@Override
		public Promise<Closure<?>> promise(Closure<?> successClosure, Closure<?> failClosure) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					List<Map<String, ?>> resultList = new ArrayList<>();
					for (Map<String, ?> map : cursor) {
						resultList.add(map);
					}
					return resultList;
				}
			}, successClosure, failClosure);
		}
	}
}
