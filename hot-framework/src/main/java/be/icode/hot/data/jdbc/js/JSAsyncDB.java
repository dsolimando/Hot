package be.icode.hot.data.jdbc.js;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.data.AsyncCollection;
import be.icode.hot.data.Collection;
import be.icode.hot.data.DB;
import be.icode.hot.data.jdbc.AbstractAsyncDB.JoinableAsyncCollection;
import be.icode.hot.data.jdbc.JoinableCollection;


public class JSAsyncDB extends be.icode.hot.data.scripting.JSAsyncDB {

	public JSAsyncDB(DB<NativeObject> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop, Scriptable globalscope) {
		super(db, blockingTasksThreadPool, eventLoop, globalscope);
	}

	@Override
	protected AsyncCollection<NativeFunction, NativeObject> buildAsyncCollection(
			Collection<NativeObject> collection) {
		return new JSJoinableAsyncCollection(collection);
	}
	
	private class JSJoinableAsyncCollection extends be.icode.hot.data.scripting.JSAsyncDB.JSAsyncCollection implements JoinableAsyncCollection<NativeFunction, NativeObject> {

		public JSJoinableAsyncCollection(Collection<NativeObject> collection) {
			super(collection);
		}

		@Override
		public AsyncCollection<NativeFunction, NativeObject> join(List<String> joinPaths) {
			return new JSAsyncCollection(((JoinableCollection<NativeObject>) collection).join(joinPaths));
		}
	}
}
