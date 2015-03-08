package be.solidx.hot.data.jdbc;

import java.util.Map;

import be.solidx.hot.data.CollectionMetadata;

@SuppressWarnings("rawtypes")
public interface DB<T extends Map> extends be.solidx.hot.data.DB<T> {

	CollectionMetadata getCollectionMetadata(String name);
	
	public JoinableCollection<T> getCollection(String name);
}
