package be.icode.hot.gae.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db4o.ObjectContainer;
import com.db4o.ext.Db4oRecoverableException;
import com.db4o.query.Predicate;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

public class Db4ODataStore implements DatastoreService{

	private ObjectContainer objectContainer;
	
	public Db4ODataStore(ObjectContainer objectContainer) {
		this.objectContainer = objectContainer;
	}
	
	@Override
	public void delete(Iterable<Key> keys) {
		for (Key key : keys) {
			Entity entity = get(key);
			objectContainer.delete(entity);
		}
	}

	@Override
	public void delete(Key... keys) {
		delete(Arrays.asList(keys));
	}

	@Override
	public Map<Key, Entity> get(Iterable<Key> keys) {
		Map<Key, Entity> entities = new HashMap<Key, Entity>();
		for (Key key : keys) {
			entities.put(key, get(key));
		}
		return entities;
	}

	@SuppressWarnings("serial")
	@Override
	public Entity get(final Key key) {
		List<Entity> entities = objectContainer.query(new Predicate<Entity>() {
			public boolean match(Entity entity) {
				return entity.getKey().getId() == key.getId();
			}
		});
		return entities.get(0);
	}

	@Override
	public List<Key> put(Iterable<Entity> entities) {
		List<Key> keys = new ArrayList<Key>();
		for (Entity entity : entities) {
			keys.add(put(entity));
		}
		return keys;
	}

	@Override
	public Key put(Entity entity) {
		try {
			Entity stored = get(entity.getKey());
			for (String property : entity.getProperties().keySet()) {
				stored.setProperty(property, entity.getProperty(property));
			}
			//throw new RuntimeException("Entity already exist "+entity.getKey());
		} catch (Db4oRecoverableException e) {
		}
		objectContainer.ext().store(entity,2);
		return entity.getKey();
	}

	@Override
	public PreparedQuery prepare(Query query) {
		Db4OPreparedQuery db4oPreparedQuery = new Db4OPreparedQuery(query, objectContainer);
		return db4oPreparedQuery;
	}

	public ObjectContainer getObjectContainer() {
		return objectContainer;
	}

	@Override
	public void delete(Transaction transaction, Iterable<Key> keys) {
		delete(keys);
	}

	@Override
	public void delete(Transaction transaction, Key... keys) {
		delete(keys);
	}

	@Override
	public Map<Key, Entity> get(Transaction transaction, Iterable<Key> keys) {
		return get(keys);
	}

	@Override
	public Entity get(Transaction transaction, Key key) {
		return get(key);
	}

	@Override
	public List<Key> put(Transaction transaction, Iterable<Entity> entities) {
		return put(entities);
	}

	@Override
	public Key put(Transaction transaction, Entity entity) {
		return put(entity);
	}

	@Override
	public Transaction beginTransaction() {
		return new TransactionImpl();
	}
}
