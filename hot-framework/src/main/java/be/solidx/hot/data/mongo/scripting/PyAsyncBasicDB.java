package be.solidx.hot.data.mongo.scripting;

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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.MongoAsyncCollection;
import be.solidx.hot.data.scripting.PyAsyncDB;
import be.solidx.hot.promises.Promise;

public class PyAsyncBasicDB extends PyAsyncDB {

	public PyAsyncBasicDB(DB<PyDictionary> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	protected AsyncCollection<PyFunction, PyDictionary> buildAsyncCollection(Collection<PyDictionary> collection) {
		return new PyAsyncBasicCollection(collection);
	}
	
	public ObjectId ObjectId(String id) {
		return ((BasicDB<PyDictionary>)db).ObjectId(id);
	}
	
	public class PyAsyncBasicCollection extends PyAsyncCollection implements MongoAsyncCollection<PyFunction, PyDictionary> {

		public PyAsyncBasicCollection(Collection<PyDictionary> collection) {
			super(collection);
		}

        @Override
        public Promise<PyFunction> count() {
            return count(new PyDictionary());
        }

        @Override
        public Promise<PyFunction> count(PyFunction successCallback) {
            return count(successCallback,null);
        }

        @Override
        public Promise<PyFunction> count(PyFunction successCallback, PyFunction errorCallback) {
            return deferredBlockingCall(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    return ((BasicDB<PyDictionary>.BasicCollection) collection).count();
                }
            }, successCallback, errorCallback);
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
		
		@Override
		public Promise<PyFunction> update(final PyDictionary q, final PyDictionary d, final boolean upsert, final boolean multi) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<PyDictionary>.BasicCollection) collection).update(q, d, upsert, multi);
				}
			}, null, null);
		}
	}
}
