package be.solidx.hot.data.mongo.scripting;

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

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.MongoAsyncCollection;
import be.solidx.hot.data.scripting.JSAsyncDB;
import be.solidx.hot.promises.Promise;

public class JSAsyncBasicDB extends JSAsyncDB {

	public JSAsyncBasicDB(DB<NativeObject> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop, Scriptable globalscope) {
		super(db, blockingTasksThreadPool, eventLoop, globalscope);
	}
	
	public ObjectId ObjectId(String id) {
		return ((BasicDB<NativeObject>)db).ObjectId(id);
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
        public Promise<NativeFunction> count() {
            return count(new NativeObject());
        }

        @Override
        public Promise<NativeFunction> count(NativeFunction successCallback) {
            return count(successCallback,null);
        }

        @Override
        public Promise<NativeFunction> count(NativeFunction successCallback, NativeFunction errorCallback) {
            return deferredBlockingCall(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    return ((BasicDB<NativeObject>.BasicCollection) collection).count();
                }
            }, successCallback, errorCallback);
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

		@Override
		public Promise<NativeFunction> update(final NativeObject q, final NativeObject d, final boolean upsert, final boolean multi) {
			return deferredBlockingCall(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ((BasicDB<NativeObject>.BasicCollection) collection).update(q, d, upsert, multi);
				}
			}, null, null);
		}
	}
}
