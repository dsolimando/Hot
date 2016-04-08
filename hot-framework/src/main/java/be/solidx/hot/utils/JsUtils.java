package be.solidx.hot.utils;

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
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

public class JsUtils {

	public static NativeObject mapToNativeObject (Map<?, ?> map) {
		NativeObject nativeObject = new NativeObject();
		for (Object key : map.keySet()) {
			if (map.get(key) instanceof Map<?, ?>) {
				nativeObject.put(key.toString(),nativeObject,mapToNativeObject((Map<?, ?>) map.get(key)));
			} else if (map.get(key) instanceof List<?>) {
				nativeObject.put(key.toString(),nativeObject,listToNativeArray((List<?>) map.get(key)));
			} else {
				nativeObject.put(key.toString(),nativeObject,map.get(key));
			}
		}
		return nativeObject;
	}
	
	public static NativeArray listToNativeArray (List<?> list) {
		List<Object> nativeObjects = new ArrayList<Object>();
		for (Object object : list) {
			if (object instanceof Map<?, ?>) {
				nativeObjects.add(mapToNativeObject((Map<?, ?>) object));
			} else if (object instanceof List<?>) {
				nativeObjects.add(listToNativeArray((List<?>) object));
			} else {
				nativeObjects.add(object);
			}
		}
		return new NativeArray(nativeObjects.toArray());
	}
}
