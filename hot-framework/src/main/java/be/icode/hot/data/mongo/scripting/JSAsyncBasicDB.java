package be.icode.hot.data.mongo.scripting;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.data.AsyncCollection;
import be.icode.hot.data.Collection;
import be.icode.hot.data.DB;
import be.icode.hot.data.mongo.BasicDB;
import be.icode.hot.data.mongo.MongoAsyncCollection;
import be.icode.hot.data.scripting.JSAsyncDB;
import be.icode.hot.promises.Promise;

public class JSAsyncBasicDB extends JSAsyncDB {

	public JSAsyncBasicDB(DB<NativeObject> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop, Scriptable globalscope) {
		super(db, blockingTasksThreadPool, eventLoop, globalscope);
	}

	@Override
	protected AsyncCollection<NativeFunction, NativeObject> buildAsyncCollection(Collection<NativeObject> collection) {
		return new JSAsyncBasicCollection(collection);
	}
	
	public class JSAsyncBasicCollection extends JSAsyncCollection implements MongoAsyncCollection<NativeFunction, NativeObject> {

		public JSAsyncBasicCollection(Collection<NativeObject> collection) {
			super(collection);
		}

		@Override
		public Promise<NativeFunction> save(NativeObject t) {
			return save(t,null);
		}

		@Override
		public Promise<NativeFunction> save(NativeObject t, NativeFunction successCallback) {
			return save(t, successCallback,null);
		}

		@Override
		public Promise<NativeFunction> save(final NativeObject t, NativeFunction successCallback, NativeFunction errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return ((BasicDB<NativeObject>.BasicCollection) collection).save(t);
				}
			}, successCallback, errorCallback);
		}

		@Override
		public Promise<NativeFunction> runCommand(String command, NativeObject t) {
			return runCommand(command, t, null);
		}

		@Override
		public Promise<NativeFunction> runCommand(String command, NativeObject t, NativeFunction successCallback) {
			return runCommand(command, t, successCallback, null);
		}

		@Override
		public Promise<NativeFunction> runCommand(final String command, final NativeObject t, NativeFunction successCallback, NativeFunction errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<NativeObject>.BasicCollection) collection).runCommand(command, t);
				}
			}, successCallback, errorCallback);
		}
	}
}
