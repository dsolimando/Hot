package be.icode.hot.data.gae.query.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("rawtypes")
public abstract class AbstractEntityToMapTransformer<T extends Map> implements EntityTransformer<T> {

	@SuppressWarnings("unchecked")
	@Override
	public T toMap(Entity entity) {
		T map = buildMap();
		map.put(ID, entity.getKey().getId());
		return map;
	}

	@Override
	public Entity fromMap(T map, String kind) {
		Key key = null;
		if (map.containsKey(ID)) {
			Object idObject = map.get(ID);
			if (idObject instanceof Long) {
				key = KeyFactory.createKey(kind, (Long) map.get(ID));
			} else if (idObject instanceof Integer) {
				key = KeyFactory.createKey(kind, (Integer) map.get(ID));
			} else {
				key = KeyFactory.createKey(kind, UUID.randomUUID().getMostSignificantBits());
			}
			map.remove(ID);
		}
		Entity entity = new Entity(key);
		return entity;
	}
	
	@Override
	public Iterator<T> toIterator(List<Entity> entities) {
		ArrayList<T> results = new ArrayList<T>();
		for (Entity entity : entities) {
			results.add(toMap(entity));
		}
		return results.iterator();
	}

	abstract protected T buildMap ();
	
	abstract protected void put (T map, String key, Object value);
}
