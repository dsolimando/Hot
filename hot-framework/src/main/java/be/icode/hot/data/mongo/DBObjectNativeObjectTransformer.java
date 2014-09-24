package be.icode.hot.data.mongo;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBObjectNativeObjectTransformer implements DBObjectTransformer<NativeObject> {

	@Override
	public NativeObject fromDBObject(BasicDBObject dbObject) {
		return dbObjectToNativeObject(dbObject);
	}

	@Override
	public NativeObject put(NativeObject nativeObject, String key, Object value) {
		nativeObject.put(key, nativeObject, value);
		return nativeObject;
	}
	
	public static NativeObject dbObjectToNativeObject (DBObject dbObject) {
		NativeObject nativeObject = new NativeObject();
		if (dbObject == null) {
			return null;
		}
		for (String key : dbObject.keySet()) {
			if (dbObject.get(key) instanceof List<?>) {
				nativeObject.put(key.toString(),nativeObject,listToNativeArray((List<?>) dbObject.get(key)));
			} else if (dbObject.get(key) instanceof DBObject) {
				nativeObject.put(key.toString(),nativeObject,dbObjectToNativeObject((DBObject) dbObject.get(key)));
			}  else {
				nativeObject.put(key.toString(),nativeObject,key.equals("_id")?dbObject.get(key).toString():dbObject.get(key));
			}
		}
		return nativeObject;
	}
	
	public static NativeArray listToNativeArray (List<?> list) {
		List<Object> nativeObjects = new ArrayList<Object>();
		for (Object object : list) {
			if (object instanceof List<?>) {
				nativeObjects.add(listToNativeArray((List<?>) object));
			} else if (object instanceof DBObject) {
				nativeObjects.add(dbObjectToNativeObject((DBObject) object));
			} else {
				nativeObjects.add(object);
			}
		}
		return new NativeArray(nativeObjects.toArray());
	}
}
