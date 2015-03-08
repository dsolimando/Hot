package be.solidx.hot.data;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface DB<T extends Map> {

	Collection<T> getCollection (String name);
	
	List<String> listCollections();
	
	List<String> getPrimaryKeys(String collection);
}
