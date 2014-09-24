package be.icode.hot.data.gae.query.util;

import org.mozilla.javascript.NativeObject;

public class EntityToNativeObjectTransformer extends AbstractEntityToMapTransformer<NativeObject> {

	@Override
	protected NativeObject buildMap() {
		return new NativeObject();
	}

	@Override
	protected void put(NativeObject map, String key, Object value) {
		map.put(key, map, value);
	}
}
