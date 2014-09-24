package com.google.appengine.api.datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entity {
	
	private Key key;
	
	private String kind;
	
	Map<String, Object> properties = new HashMap<String, Object>();

	public Entity(Key key) {
		this.key = key;
		this.kind = key.getKind();
	}

	public Entity(String kind) {
		key = KeyFactory.createKey (kind, Math.abs(UUID.randomUUID().getLeastSignificantBits()));
		this.kind = kind;
	}

	public Key getKey() {
		return key;
	}
	
	public String getKind() {
		return kind;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public Object getProperty (String propertyName) {
		return properties.get(propertyName);
	}
	
	public boolean hasProperty (String propertyName) {
		return properties.keySet().contains(propertyName);
	}
	
	public void removeProperty (String propertyName) {
		properties.remove(propertyName);
	}
	
	public void setProperty (String propertyName, Object value) {
		properties.put(propertyName, value);
	}
}
