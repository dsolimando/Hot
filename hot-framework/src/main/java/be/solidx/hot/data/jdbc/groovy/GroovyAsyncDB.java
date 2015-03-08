package be.solidx.hot.data.jdbc.groovy;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.CollectionMetadata;
import be.solidx.hot.data.jdbc.AsyncDB;
import be.solidx.hot.data.jdbc.JoinableCollection;
import be.solidx.hot.data.jdbc.AbstractAsyncDB.JoinableAsyncCollection;
import be.solidx.hot.data.jdbc.groovy.DB;


public class GroovyAsyncDB extends be.solidx.hot.data.scripting.GroovyAsyncDB implements AsyncDB<Closure<?>, Map<String, Object>>{

	public GroovyAsyncDB(DB db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	protected AsyncCollection<Closure<?>, Map<String, Object>> buildAsyncCollection(
			Collection<Map<String, Object>> collection) {
		return new GroovyJoinableAsyncCollection(collection);
	}

	private class GroovyJoinableAsyncCollection 
		extends be.solidx.hot.data.scripting.GroovyAsyncDB.GroovyAsyncCollection 
		implements JoinableAsyncCollection<Closure<?>, Map<String,Object>> {

		public GroovyJoinableAsyncCollection(Collection<Map<String, Object>> collection) {
			super(collection);
		}

		@Override
		public AsyncCollection<Closure<?>, Map<String, Object>> join(List<String> joinPaths) {
			return new GroovyAsyncCollection(((JoinableCollection<Map<String,Object>>)collection).join(joinPaths));
		}
	}
	

	@Override
	public CollectionMetadata getCollectionMetadata(String name) {
		return ((be.solidx.hot.data.jdbc.DB)db).getCollectionMetadata(name);
	}
}
