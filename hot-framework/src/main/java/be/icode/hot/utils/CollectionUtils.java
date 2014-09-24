package be.icode.hot.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Multimap;

public class CollectionUtils {

	public static Map<String, Object> flat (Multimap<String, Object> multimap) {
		Map<String, Object> flattenMap = new LinkedHashMap<String, Object>();
		for (String key : multimap.keySet()) {
			int i = 0;
			for (Object object : multimap.get(key)) {
				flattenMap.put(key.replaceAll("\\.", "_") + "_" + i++, object);
			}
		}
		return flattenMap;
	}
	
	public static  byte[] merge (byte[] a, byte[] b) {
		int total = a.length + b.length;
		byte[] c = new byte[total];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i];
		}	
		for (int i = 0; i < b.length; i++) {
			c[a.length+i] = b[i];
		}
		return c;
	}
}
