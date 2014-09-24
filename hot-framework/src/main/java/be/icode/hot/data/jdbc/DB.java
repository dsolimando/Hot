package be.icode.hot.data.jdbc;

import java.util.Map;

import be.icode.hot.data.CollectionMetadata;

@SuppressWarnings("rawtypes")
public interface DB<T extends Map> extends be.icode.hot.data.DB<T> {

	CollectionMetadata getCollectionMetadata(String name);
	
	public JoinableCollection<T> getCollection(String name);
}
