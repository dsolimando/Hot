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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.data.AbstractAsyncDB;
import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.AsyncCursor;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.Cursor;
import be.solidx.hot.data.DB;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.js.JSDeferred;

public class JSAsyncDB extends AbstractAsyncDB<NativeFunction, NativeObject> {

	Scriptable globalscope;
	
	public JSAsyncDB(DB<NativeObject> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop, Scriptable globalscope) {
		super(db, blockingTasksThreadPool, eventLoop);
		this.globalscope = globalscope;
	}

	@Override
	public Object executeClosure(NativeFunction closure, Object... args) {
		JSClosure jsClosure = new JSClosure(closure, globalscope);
		return jsClosure.call(args);
	}

	@Override
	protected Deferred<NativeFunction> buildDeferred() {
		return new JSDeferred(globalscope);
	}

	@Override
	protected AsyncCursor<NativeFunction, NativeObject> buildAsyncCursor(Cursor<NativeObject> cursor) {
		return new JsAsyncCursor(cursor);
	}

	@Override
	protected AsyncCollection<NativeFunction, NativeObject> buildAsyncCollection(Collection<NativeObject> collection) {
		return new JSAsyncCollection(collection);
	}

	public class JSAsyncCollection extends AbstractAsyncCollection {

		public JSAsyncCollection(Collection<NativeObject> collection) {
			super(collection);
		}
	}
	
	public class JsAsyncCursor extends AbstractAsyncCursor {

		public JsAsyncCursor(Cursor<NativeObject> cursor) {
			super(cursor);
		}

		@Override
		public Promise<NativeFunction> promise(NativeFunction successClosure, NativeFunction failClosure) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					List<Object> results = new ArrayList<>();
					for (Object it : cursor) {
						results.add(it);
					}
					return new NativeArray(results.toArray());
				}
			}, successClosure, failClosure);
		}
	}
}
