package be.solidx.hot.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

public class JsUtils {

	public static NativeObject mapToNativeObject (Map<?, ?> map) {
		NativeObject nativeObject = new NativeObject();
		for (Object key : map.keySet()) {
			if (map.get(key) instanceof Map<?, ?>) {
				nativeObject.put(key.toString(),nativeObject,mapToNativeObject((Map<?, ?>) map.get(key)));
			} else if (map.get(key) instanceof List<?>) {
				nativeObject.put(key.toString(),nativeObject,listToNativeArray((List<?>) map.get(key)));
			} else {
				nativeObject.put(key.toString(),nativeObject,map.get(key));
			}
		}
		return nativeObject;
	}
	
	public static NativeArray listToNativeArray (List<?> list) {
		List<Object> nativeObjects = new ArrayList<Object>();
		for (Object object : list) {
			if (object instanceof Map<?, ?>) {
				nativeObjects.add(mapToNativeObject((Map<?, ?>) object));
			} else if (object instanceof List<?>) {
				nativeObjects.add(listToNativeArray((List<?>) object));
			} else {
				nativeObjects.add(object);
			}
		}
		return new NativeArray(nativeObjects.toArray());
	}
}
