package be.solidx.hot;

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
