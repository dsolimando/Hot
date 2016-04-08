package be.solidx.hot.js;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.utils.ScriptMapConverter;

public class JsMapConverter implements ScriptMapConverter<NativeObject> {

	public NativeObject toScriptMap (Map<?, ?> map) {
		NativeObject javascriptMap = new NativeObject();
		for (Object key: map.keySet()) {
			Object o = map.get(key);
			if (key instanceof String) {
				if (o instanceof List) {
					List<Object> lo = new ArrayList<>();
					for (Object ob : (List<?>)o) {
						if (ob instanceof Map<?,?>)
							lo.add(toScriptMap((Map<?, ?>) ob));
						else
							lo.add(ob);
					}
					javascriptMap.put(key.toString(), javascriptMap, new NativeArray(lo.toArray()));
				} else if(o instanceof Map<?,?>) {
					javascriptMap.put(key.toString(), javascriptMap, toScriptMap((Map<?, ?>) o));
				} else {
					javascriptMap.put(key.toString(), javascriptMap,o);
				}
			}
		}
		return javascriptMap;
	}
	
	public List<Map<?, ?>> toListMap(NativeArray array) {
		List<Map<?, ?>> list  = new ArrayList<>();
		for (Object object : array) {
			if (object instanceof NativeObject)
				list.add(toMap((NativeObject) object));
			else if (object instanceof NativeArray)
				list.add((Map<?, ?>) toListMap(array));
		}
		return list;
	}
	
	
	public Map<?,?> toMap(NativeObject nativeObject) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (Object key: nativeObject.keySet()) {
			if (!(key instanceof String)) continue;
			Object o = nativeObject.get(key);
			if (o instanceof NativeObject) {
				map.put((String) key,toMap((NativeObject) o));
			} else {
				map.put((String) key, o);
			}
		}
		return map;
	}
	
	public NativeObject multiValueMapToMapList (Map<String, MultiValueMap<String, String>> matrixVariables) {
		NativeObject map = new NativeObject();
		if (matrixVariables == null) return map;
		for (MultiValueMap<String, String> vars : matrixVariables.values()) {
			for (String name : vars.keySet()) {
				if (vars.size() > 1) {
					NativeArray valueList = new NativeArray(vars.size());
					for (String value : vars.get(name)) {
						valueList.add(value);
					}
					map.put(name, map, valueList);
				} else if (vars.size() == 1) {
					map.put(name, vars.get(name).get(0));
				}
			}
		}
		return map;
	}

	@Override
	public NativeObject httpHeadersToMap(WebRequest webRequest) {
		NativeObject headers = new NativeObject();
		Iterator<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasNext()) {
			String headerName = headerNames.next();
			NativeArray values = new NativeArray(webRequest.getHeaderValues(headerName));
			headers.put(headerName, headers, new NativeArray(values.toArray()));
		}
		return headers;
	}
	
	@Override
	public NativeObject httpHeadersToMap(HttpServletRequest webRequest) {
		NativeObject headers = new NativeObject();
		Enumeration<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			Enumeration<String> headerValues = webRequest.getHeaders(headerName);
			List<String> values = new ArrayList<>();
			while (headerValues.hasMoreElements()) {
				values.add(headerValues.nextElement());
			}
			headers.put(headerName, headers, new NativeArray(values.toArray()));
		}
		return headers;
	}
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsMapConverter converter = new JsMapConverter();
		NativeObject nativeObject = converter.toScriptMap(mapper.readValue("{\"extraInfos\":[{\"code\":\"ALLOC_ETP\",\"description\":\"Temps alloué à la gestion du DU employeur (100%=temps plein)\",\"valeurs\":[\"25\"]}]}".getBytes(), Map.class));
		
	}
}
