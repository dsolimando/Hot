package com.google.appengine.api.datastore;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Key implements Serializable {
	
	private long id;
	
	private String kind;
	
	private String name;
	
	private String namespace;
	
	Key (String kind, long id) {
		this.id = id;
		this.kind = kind;
	}

	public long getId () {
		return id;
	}
	
	public String getKind () {
		return kind;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public boolean equals (Object object) {
		if (object instanceof Key) {
			Key key = (Key) object;
			return key.getId() == id && key.getKind().equals(kind);
		} 
		return false;
	}
}
