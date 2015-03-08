package be.solidx.hot.data.mongo;

import java.util.ArrayList;
import java.util.List;

import org.python.core.PyDictionary;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBObjectPyDictionaryTransformer implements DBObjectTransformer<PyDictionary> {

	@Override
	public PyDictionary fromDBObject(BasicDBObject dbObject) {
		return dbObjectToPyDictionary(dbObject);
	}

	@Override
	public PyDictionary put(PyDictionary t, String key, Object value) {
		t.put(key, value);
		return t;
	}
	
	public PyDictionary dbObjectToPyDictionary (DBObject dbObject) {
		if (dbObject == null) {
			return null;
		}
		PyDictionary pyDictionary = new PyDictionary();
		for (String key : dbObject.keySet()) {
			if (dbObject.get(key) instanceof List<?>) {
				pyDictionary.put(key.toString(),transformList((List<?>) dbObject.get(key)));
			} else if (dbObject.get(key) instanceof DBObject) {
				pyDictionary.put(key.toString(),dbObjectToPyDictionary((DBObject) dbObject.get(key)));
			} else {
				pyDictionary.put(key.toString(), key.equals("_id")?dbObject.get(key).toString():dbObject.get(key));
			}
		}
		return pyDictionary;
	}
	
	public List<Object> transformList (List<?> list) {
		List<Object> transformedList = new ArrayList<Object>();
		for (Object object : list) {
			if (object instanceof List<?>) {
				transformedList.add(transformList((List<?>) object));
			} else if (object instanceof DBObject) {
				transformedList.add(dbObjectToPyDictionary((DBObject) object));
			} else {
				transformedList.add(object);
			}
		}
		return transformedList;
	}
}
