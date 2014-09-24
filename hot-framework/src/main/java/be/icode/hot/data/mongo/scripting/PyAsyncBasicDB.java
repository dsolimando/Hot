package be.icode.hot.data.mongo.scripting;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.icode.hot.data.AsyncCollection;
import be.icode.hot.data.Collection;
import be.icode.hot.data.DB;
import be.icode.hot.data.mongo.BasicDB;
import be.icode.hot.data.mongo.MongoAsyncCollection;
import be.icode.hot.data.scripting.PyAsyncDB;
import be.icode.hot.promises.Promise;

public class PyAsyncBasicDB extends PyAsyncDB {

	public PyAsyncBasicDB(DB<PyDictionary> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	protected AsyncCollection<PyFunction, PyDictionary> buildAsyncCollection(Collection<PyDictionary> collection) {
		return new PyAsyncBasicCollection(collection);
	}
	
	public class PyAsyncBasicCollection extends PyAsyncCollection implements MongoAsyncCollection<PyFunction, PyDictionary> {

		public PyAsyncBasicCollection(Collection<PyDictionary> collection) {
			super(collection);
		}

		@Override
		public Promise<PyFunction> save(PyDictionary t) {
			return save(t,null);
		}

		@Override
		public Promise<PyFunction> save(PyDictionary t, PyFunction successCallback) {
			return save(t, successCallback,null);
		}

		@Override
		public Promise<PyFunction> save(final PyDictionary t, PyFunction successCallback, PyFunction errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return ((BasicDB<PyDictionary>.BasicCollection) collection).save(t);
				}
			}, successCallback, errorCallback);
		}

		@Override
		public Promise<PyFunction> runCommand(String command, PyDictionary t) {
			return runCommand(command, t, null);
		}

		@Override
		public Promise<PyFunction> runCommand(String command, PyDictionary t, PyFunction successCallback) {
			return runCommand(command, t, successCallback, null);
		}

		@Override
		public Promise<PyFunction> runCommand(final String command, final PyDictionary t, PyFunction successCallback, PyFunction errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<PyDictionary>.BasicCollection) collection).runCommand(command, t);
				}
			}, successCallback, errorCallback);
		}
	}
}
