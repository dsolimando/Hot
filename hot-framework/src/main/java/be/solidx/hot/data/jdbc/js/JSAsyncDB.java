package be.solidx.hot.data.jdbc.js;

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

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.jdbc.JoinableCollection;
import be.solidx.hot.data.jdbc.AbstractAsyncDB.JoinableAsyncCollection;


public class JSAsyncDB extends be.solidx.hot.data.scripting.JSAsyncDB {

	public JSAsyncDB(DB<NativeObject> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop, Scriptable globalscope) {
		super(db, blockingTasksThreadPool, eventLoop, globalscope);
	}

	@Override
	protected AsyncCollection<NativeFunction, NativeObject> buildAsyncCollection(
			Collection<NativeObject> collection) {
		return new JSJoinableAsyncCollection(collection);
	}
	
	private class JSJoinableAsyncCollection extends be.solidx.hot.data.scripting.JSAsyncDB.JSAsyncCollection implements JoinableAsyncCollection<NativeFunction, NativeObject> {

		public JSJoinableAsyncCollection(Collection<NativeObject> collection) {
			super(collection);
		}

		@Override
		public AsyncCollection<NativeFunction, NativeObject> join(List<String> joinPaths) {
			return new JSAsyncCollection(((JoinableCollection<NativeObject>) collection).join(joinPaths));
		}
	}
}
