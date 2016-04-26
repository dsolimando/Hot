package be.solidx.hot.python;

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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyString;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.utils.ScriptMapConverter;

public class PyDictionaryConverter implements ScriptMapConverter<PyDictionary> {

	@Override
	public PyDictionary toScriptMap (Map<?, ?> map) {
		
		if (map == null) return null;
		
		PyDictionary pyDictionary = new PyDictionary();
		for (Object key : map.keySet()) {
			Object object = map.get(key);
			if (object instanceof Object []) {
				Object[] oa = (Object[]) object;
				pyDictionary.put(key, oa[0]);
			} else {
				pyDictionary.put(key, object);
			}
		}
		return pyDictionary;
	}
	
	@Override
	public Map<String, Object> toMap(PyDictionary dictionary) {
		
		if (dictionary == null) return null;
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (Object key: dictionary.keySet()) {
			if (!(key instanceof String)) continue;
			Object o = dictionary.get(key);
			if (o instanceof PyDictionary) {
				map.put((String) key,toMap((PyDictionary) o));
			} else {
				map.put((String) key, o);
			}
		}
		return map;
	}
	
	@Override
	public PyDictionary multiValueMapToMapList (Map<String, MultiValueMap<String, String>> matrixVariables) {
		
		if (matrixVariables == null) return null;
		
		PyDictionary map = new PyDictionary();
		
		for (MultiValueMap<String, String> vars : matrixVariables.values()) {
			for (String name : vars.keySet()) {
				if (vars.size() > 1) {
					PyList valueList = new PyList();
					for (String value : vars.get(name)) {
						valueList.add(value);
					}
					map.put(name, valueList);
				} else if (vars.size() == 1) {
					map.put(name, vars.get(name).get(0));
				}
			}
		}
		return map;
	}
	
	@Override
	public PyDictionary httpHeadersToMap(WebRequest webRequest) {
		
		if (webRequest == null) return null;
		
		PyDictionary headers = new PyDictionary();
		Iterator<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasNext()) {
			String headerName = headerNames.next();
			PyList pyList = new PyList();
			for (String headerValue : webRequest.getHeaderValues(headerName)) {
				pyList.add(new PyString(headerValue));
			}
			headers.put(headerName, pyList);
		}
		return headers;
	}
	
	@Override
	public PyDictionary httpHeadersToMap(HttpServletRequest webRequest) {
		
		if (webRequest == null) return null;

		
		PyDictionary headers = new PyDictionary();
		Enumeration<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			PyList values = new PyList();
			Enumeration<String> headerValues = webRequest.getHeaders(headerName);
			while (headerValues.hasMoreElements()) {
				values.add(headerValues.nextElement());
			}
			headers.put(headerName, values);
		}
		return headers;
	}
}
