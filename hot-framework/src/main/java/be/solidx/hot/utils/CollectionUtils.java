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

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Multimap;

public class CollectionUtils {

	public static Map<String, Object> flat (Multimap<String, Object> multimap) {
		Map<String, Object> flattenMap = new LinkedHashMap<String, Object>();
		for (String key : multimap.keySet()) {
			int i = 0;
			for (Object object : multimap.get(key)) {
				flattenMap.put(key.replaceAll("\\.", "_") + "_" + i++, object);
			}
		}
		return flattenMap;
	}
	
	public static  byte[] merge (byte[] a, byte[] b) {
		int total = a.length + b.length;
		byte[] c = new byte[total];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i];
		}	
		for (int i = 0; i < b.length; i++) {
			c[a.length+i] = b[i];
		}
		return c;
	}
}
