package be.icode.hot.gae.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Development only class
 * 
 * Needed for closing the Db4o container
 * @author dsolimando
 *
 */
public class GaeDataStoreFactory {

	Db4ODataStore dataStore;
	
	public DatastoreService getInstance () {
		dataStore = (Db4ODataStore) DatastoreServiceFactory.getDatastoreService();
		return dataStore;
	}
	
	public void close () {
		dataStore.getObjectContainer().close();
	}
}
