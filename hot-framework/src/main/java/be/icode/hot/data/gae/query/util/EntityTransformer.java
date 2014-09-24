package be.icode.hot.data.gae.query.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("rawtypes")
public interface EntityTransformer<T extends Map> {
	
	public static final String ID = "_id";

	T toMap (Entity entity);
	
	Iterator<T> toIterator (List<Entity> entities);
	
	Entity fromMap (T t, String kind);
}
