package be.solidx.hot.data.mongo.scripting;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;

import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.Collection;
import be.solidx.hot.data.mongo.MongoAsyncCollection;
import be.solidx.hot.data.scripting.GroovyAsyncDB;
import be.solidx.hot.promises.Promise;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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

		@Override
		public Promise<Closure<?>> update(final Map<String, Object> q, final Map<String, Object> d, final boolean upsert, final boolean multi) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return  ((BasicDB<Map<String,Object>>.BasicCollection)collection).update(q, d, upsert, multi);
				}
			}, null, null);
		}
	}
}
