package be.icode.hot.data.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.data.AbstractAsyncDB;
import be.icode.hot.data.AsyncCollection;
import be.icode.hot.data.AsyncCursor;
import be.icode.hot.data.Collection;
import be.icode.hot.data.Cursor;
import be.icode.hot.data.DB;
import be.icode.hot.js.JSClosure;
import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.Promise;
import be.icode.hot.promises.js.JSDeferred;

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
