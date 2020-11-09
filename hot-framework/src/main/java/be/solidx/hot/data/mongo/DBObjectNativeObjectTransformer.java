package be.solidx.hot.data.mongo;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
