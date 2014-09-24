package com.google.appengine.api.datastore;

public interface BaseDatastoreService {
	PreparedQuery prepare(Query query);
}
