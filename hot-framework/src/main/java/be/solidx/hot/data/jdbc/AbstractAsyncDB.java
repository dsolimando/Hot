package be.solidx.hot.data.jdbc;

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
