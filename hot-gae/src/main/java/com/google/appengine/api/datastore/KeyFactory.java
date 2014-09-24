package com.google.appengine.api.datastore;

public class KeyFactory {

	public static Key createKey(String kind, long id) {
		return new Key(kind, id);
	}
}
