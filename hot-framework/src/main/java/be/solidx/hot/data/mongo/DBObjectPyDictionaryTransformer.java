package be.solidx.hot.data.mongo;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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
