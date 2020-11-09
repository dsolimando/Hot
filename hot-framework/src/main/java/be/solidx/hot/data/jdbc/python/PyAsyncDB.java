package be.solidx.hot.data.jdbc.python;

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

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.jdbc.JoinableCollection;
import be.solidx.hot.data.jdbc.AbstractAsyncDB.JoinableAsyncCollection;

public class PyAsyncDB extends be.solidx.hot.data.scripting.PyAsyncDB {

	public PyAsyncDB(DB<PyDictionary> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	protected AsyncCollection<PyFunction, PyDictionary> buildAsyncCollection(Collection<PyDictionary> collection) {
		return new PyJoinableAsyncCollection(collection);
	}
	
	private class PyJoinableAsyncCollection extends be.solidx.hot.data.scripting.PyAsyncDB.PyAsyncCollection implements JoinableAsyncCollection<PyFunction, PyDictionary> {

		public PyJoinableAsyncCollection(Collection<PyDictionary> collection) {
			super(collection);
		}

		@Override
		public AsyncCollection<PyFunction, PyDictionary> join(List<String> joinPaths) {
			return new PyAsyncCollection(((JoinableCollection<PyDictionary>) collection).join(joinPaths));
		}
		
	}
}
