package be.solidx.hot.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

public interface ScriptMapConverter<MAP> {

	MAP toScriptMap(Map<?, ?> map);
	
	Map<?, ?> toMap(MAP map);
	
	MAP multiValueMapToMapList(Map<String, MultiValueMap<String, String>> matrixVariables);

	MAP httpHeadersToMap(WebRequest webRequest);

	MAP httpHeadersToMap(HttpServletRequest webRequest);
}