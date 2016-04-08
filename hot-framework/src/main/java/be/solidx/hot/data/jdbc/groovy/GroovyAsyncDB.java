package be.solidx.hot.data.jdbc.groovy;

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
