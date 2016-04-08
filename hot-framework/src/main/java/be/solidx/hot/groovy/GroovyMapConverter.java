package be.solidx.hot.groovy;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.utils.ScriptMapConverter;

public class GroovyMapConverter implements ScriptMapConverter<Map<?, ?>>{

	@Override
	public Map<?, ?> toScriptMap(Map<?, ?> map) {
		return map;
	}

	@Override
	public Map<?, ?> toMap(Map<?, ?> map) {
		return map;
	}

	@Override
	public Map<?, ?> multiValueMapToMapList(Map<String, MultiValueMap<String, String>> matrixVariables) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (matrixVariables == null) return map;
		for (MultiValueMap<String, String> vars : matrixVariables.values()) {
			for (String name : vars.keySet()) {
				if (vars.size() > 1) {
					List<String> valueList = new ArrayList<String>();
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
	public Map<?, ?> httpHeadersToMap(WebRequest webRequest) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		Iterator<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasNext()) {
			String headerName = headerNames.next();
			List<String> values = new ArrayList<String>();
			for (String headerValue : webRequest.getHeaderValues(headerName)) {
				values.add(headerValue);
			}
			headers.put(headerName, values);
		}
		return headers;
	}
	
	@Override
	public Map<?, ?> httpHeadersToMap(HttpServletRequest webRequest) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		Enumeration<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			List<String> values = new ArrayList<String>();
			Enumeration<String> headerValues = webRequest.getHeaders(headerName);
			while (headerValues.hasMoreElements()) {
				values.add(headerValues.nextElement());
			}
			headers.put(headerName, values);
		}
		return headers;
	}
}
