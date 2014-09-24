package com.google.appengine.api.datastore;

import java.util.List;
import java.util.Map;

public interface DatastoreService extends BaseDatastoreService {

	void delete (Iterable<Key> keys);
	
	void delete (Key... keys);
	
	void delete (Transaction transaction, Iterable<Key> keys);
	
	void delete (Transaction transaction, Key... keys);
	
	Map<Key, Entity> get (Iterable<Key> keys);
	
	Entity get (Key key);
	
	List<Key> put (Iterable<Entity> entities);
	
	Key put (Entity entity);
	
	Map<Key, Entity> get (Transaction transaction, Iterable<Key> keys);
	
	Entity get (Transaction transaction, Key key);
	
	List<Key> put (Transaction transaction, Iterable<Entity> entities);
	
	Key put (Transaction transaction, Entity entity);
	
	Transaction beginTransaction();
}
