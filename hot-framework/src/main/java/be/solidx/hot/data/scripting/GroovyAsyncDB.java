package be.solidx.hot.data.scripting;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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
