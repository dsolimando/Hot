package be.solidx.hot.data.mongo;

import java.util.Map;

import com.mongodb.BasicDBObject;

@SuppressWarnings("rawtypes")
public interface DBObjectTransformer<T extends Map> {
	
	T fromDBObject (BasicDBObject dbObject);
	
	T put (T t, String key, Object value);
}
