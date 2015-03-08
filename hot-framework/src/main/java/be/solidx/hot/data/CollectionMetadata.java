package be.solidx.hot.data;

import java.util.List;

public interface CollectionMetadata {

	List<String> getColumns ();
	
	List<String> getPrimaryKeys();
}
