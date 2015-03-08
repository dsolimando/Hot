package be.solidx.hot.data.mongo.scripting;

import groovy.lang.Closure;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;

import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.Collection;
import be.solidx.hot.data.mongo.MongoAsyncCollection;
import be.solidx.hot.data.scripting.GroovyAsyncDB;
import be.solidx.hot.promises.Promise;


public class GroovyAsyncBasicDB extends GroovyAsyncDB {

	public GroovyAsyncBasicDB(BasicDB<Map<String, Object>> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}
	
	public ObjectId ObjectId(String id) {
		return ((BasicDB<Map<String, Object>>)db).ObjectId(id);
	}
	
	@Override
	protected GroovyAsyncBasicCollection buildAsyncCollection(be.solidx.hot.data.Collection<Map<String, Object>> collection) {
		return new GroovyAsyncBasicCollection((Collection<Map<String, Object>>) collection);
	}

	protected class GroovyAsyncBasicCollection 	extends GroovyAsyncCollection 
												implements MongoAsyncCollection<Closure<?>, Map<String,Object>> {

		public GroovyAsyncBasicCollection(Collection<Map<String, Object>> collection) {
			super(collection);
		}
		
		@Override
		public Promise<Closure<?>> save(Map<String,Object> t) {
			return save(t, null);
		}
		
		@Override
		public Promise<Closure<?>> save(Map<String,Object> t, Closure<?> successCallback) {
			return save(t, successCallback, null);
		}
		
		@Override
		public Promise<Closure<?>> save(final Map<String,Object> t, Closure<?> successCallback, Closure<?> errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<Map<String,Object>>.BasicCollection)collection).save(t);
				}
			}, successCallback, errorCallback);
		}
		
		@Override
		public Promise<Closure<?>> runCommand(String command, Map<String,Object> t) {
			return runCommand(command, t,null);
		}
		
		@Override
		public Promise<Closure<?>> runCommand(String command, Map<String,Object> t, Closure<?> successCallback) {
			return runCommand(command, t, successCallback, null);
		}
		
		@Override
		public Promise<Closure<?>> runCommand(final String command, final Map<String,Object> t, Closure<?> successCallback, Closure<?> errorCallback) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<Map<String,Object>>.BasicCollection)collection).runCommand(command, t);
				}
			}, successCallback, errorCallback);
		}
	}
}
