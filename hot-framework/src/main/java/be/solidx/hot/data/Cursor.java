package be.solidx.hot.data;

import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface Cursor<T extends Map> extends Iterable<T> {

	@Override
	Iterator<T> iterator();
	
	Integer count();
	
	Cursor<T> limit(Integer limit);
	
	Cursor<T> skip(Integer at);
	
	Cursor<T> sort(T sortMap);
}
