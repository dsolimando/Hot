package be.icode.hot.utils;

import java.util.concurrent.ConcurrentHashMap;

public class HotCache<F,V extends Cacheable<F>> {

	private ConcurrentHashMap<String, V> map = new ConcurrentHashMap<String, V>();
	
	public V get (String key) {
		return map.get(key);
	}
	
	public void put (V value) {
		map.put(value.getId(), value);
	}
}
