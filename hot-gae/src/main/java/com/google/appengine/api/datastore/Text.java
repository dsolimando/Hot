package com.google.appengine.api.datastore;

public class Text {

	private String value;
	
	public Text(String value) {
		super();
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public boolean equals(Object input) {
		return value.equals(input);
	}
}
