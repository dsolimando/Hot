package be.icode.hot.data.gae.query.util;

import org.python.core.PyDictionary;

public class EntityToPydictionaryTransformer extends AbstractEntityToMapTransformer<PyDictionary> {

	@Override
	protected PyDictionary buildMap() {
		return new PyDictionary();
	}

	@Override
	protected void put(PyDictionary map, String key, Object value) {
		map.put(key, value);
	}
}
