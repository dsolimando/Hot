package be.icode.hot.data.mongo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBObjectMapTransformer implements DBObjectTransformer<Map<String,Object>> {

	@Override
	public Map<String,Object> fromDBObject(BasicDBObject dbObject) {
		return dbObjectTomap(dbObject);
	}

	@Override
	public Map<String, Object> put(Map<String, Object> t, String key, Object value) {
		t.put(key, value);
		return t;
	}
	
	public Map<String, Object> dbObjectTomap (DBObject dbObject) {
		if (dbObject == null) return null;
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (String key : dbObject.keySet()) {
			if (dbObject.get(key) instanceof List<?>) {
				map.put(key.toString(),transformList((List<?>) dbObject.get(key)));
			} else if (dbObject.get(key) instanceof DBObject) {
				map.put(key.toString(),dbObjectTomap((DBObject) dbObject.get(key)));
			} else {
				map.put(key.toString(), key.equals("_id")?dbObject.get(key).toString():dbObject.get(key));
			}
		}
		return map;
	}
	
	public List<Object> transformList (List<?> list) {
		List<Object> transformedList = new ArrayList<Object>();
		for (Object object : list) {
			if (object instanceof List<?>) {
				transformedList.add(transformList((List<?>) object));
			} else if (object instanceof DBObject) {
				transformedList.add(dbObjectTomap((DBObject) object));
			} else {
				transformedList.add(object);
			}
		}
		return transformedList;
	}
}
