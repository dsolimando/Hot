package be.icode.hot.data.jdbc.python;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.icode.hot.data.AsyncCollection;
import be.icode.hot.data.Collection;
import be.icode.hot.data.DB;
import be.icode.hot.data.jdbc.AbstractAsyncDB.JoinableAsyncCollection;
import be.icode.hot.data.jdbc.JoinableCollection;

public class PyAsyncDB extends be.icode.hot.data.scripting.PyAsyncDB {

	public PyAsyncDB(DB<PyDictionary> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	protected AsyncCollection<PyFunction, PyDictionary> buildAsyncCollection(Collection<PyDictionary> collection) {
		return new PyJoinableAsyncCollection(collection);
	}
	
	private class PyJoinableAsyncCollection extends be.icode.hot.data.scripting.PyAsyncDB.PyAsyncCollection implements JoinableAsyncCollection<PyFunction, PyDictionary> {

		public PyJoinableAsyncCollection(Collection<PyDictionary> collection) {
			super(collection);
		}

		@Override
		public AsyncCollection<PyFunction, PyDictionary> join(List<String> joinPaths) {
			return new PyAsyncCollection(((JoinableCollection<PyDictionary>) collection).join(joinPaths));
		}
		
	}
}
