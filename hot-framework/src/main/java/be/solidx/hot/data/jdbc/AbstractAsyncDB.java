package be.solidx.hot.data.jdbc;

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
import java.util.Map;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.data.Collection;
import be.solidx.hot.data.CollectionMetadata;


public abstract class AbstractAsyncDB<CLOSURE,T extends Map<?,?>> extends be.solidx.hot.data.AbstractAsyncDB<CLOSURE, T> implements AsyncDB<CLOSURE, T>{

	public AbstractAsyncDB(DB<T> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	@Override
	public CollectionMetadata getCollectionMetadata(String name) {
		return ((DB<T>)db).getCollectionMetadata(name);
	}
	
	@Override
	public JoinableAsyncCollection<CLOSURE, T> getCollection(String name) {
		return buildAsyncCollection(db.getCollection(name));
	}
	
	@Override
	protected abstract JoinableAsyncCollection<CLOSURE, T> buildAsyncCollection(Collection<T> collection);

	public interface JoinableAsyncCollection<CLOSURE,T extends Map<?, ?>> extends AsyncCollection<CLOSURE, T> {
		AsyncCollection<CLOSURE,T> join(List<String> joinPaths);
	}
}
