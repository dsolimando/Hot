package com.google.appengine.api.datastore;

import java.util.concurrent.Future;

public interface Transaction {

	void commit ();
	
	Future<Void> commitAsync();
	
	String getApp ();
	
	String getId ();
	
	boolean isActive ();
	
	void rollback();
	
	Future<Void> rollbackAsync() ;
}
