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
