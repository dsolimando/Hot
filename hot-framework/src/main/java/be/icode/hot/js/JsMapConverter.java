package be.icode.hot.js;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

import be.icode.hot.utils.ScriptMapConverter;

public class JsMapConverter implements ScriptMapConverter<NativeObject> {

	public NativeObject toScriptMap (Map<?, ?> map) {
		NativeObject javascriptMap = new NativeObject();
		for (Object key: map.keySet()) {
			Object o = map.get(key);
			if (key instanceof String) {
				if (o instanceof Object[]) {
					Object[] oa = (Object[]) o;
					javascriptMap.put(key.toString(), javascriptMap,oa[0]);
				} else {
					javascriptMap.put(key.toString(), javascriptMap,o);
				}
			}
		}
		return javascriptMap;
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
}
