package be.solidx.hot;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;

public class DataConverter {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedMultiValueMap<String, String> toMultiValueMap (Map map) {
		LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		for (Object key : map.keySet()) {
			if (!(key instanceof String)) continue;
			Object value = map.get(key);
			if (value instanceof List) {
				List<Object> values = (List<Object>) value;
				List<String> sValues = new ArrayList<>();
				for (Object entry : values) {
					sValues.add(entry.toString());
				}
				multiValueMap.put((String) key, sValues);
			} else if (value instanceof Map) { 
				for (Object subKey : ((Map) value).keySet()) {
					multiValueMap.put(key+ "[" + subKey + "]", Arrays.asList(((Map) value).get(subKey).toString()));
				}
			} else {
				multiValueMap.put((String)key, Arrays.asList(value.toString()));
			}
		}
		return multiValueMap;
	}
}
