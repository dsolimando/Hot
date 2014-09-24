package com.google.appengine.api.datastore;

import be.icode.hot.gae.datastore.Db4ODataStore;

import com.db4o.Db4oEmbedded;

public class DatastoreServiceFactory {

	public static DatastoreService getDatastoreService () {
		return new Db4ODataStore(Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(),".db4o_db"));
	}
}
