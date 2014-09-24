package be.icode.hot.data.gae.query.util;

import java.util.HashMap;
import java.util.Map;

public class EntityToMapTransformerImpl extends AbstractEntityToMapTransformer<Map<String, Object>> {

	@Override
	protected Map<String,Object> buildMap() {
		return new HashMap<String, Object>();
	}

	@Override
	protected void put(Map<String,Object> map, String key, Object value) {
		map.put(key, value);
	}
}
