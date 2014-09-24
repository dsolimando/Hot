package com.google.appengine.api.datastore;

public class FetchOptions {
	
	private static final int DEFAULT_LIMIT = 100;
	
	private static final int DEFAULT_OFFSET = 0;

	private int limit;
	
	private int offset;
	
	public int getLimit() {
		return limit;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public FetchOptions limit(int limit) {
		this.limit = limit;
		return this;
	}
	
	public FetchOptions offset (int offset) {
		this.offset = offset;
		return this;
	}
	
	public static class Builder {
		public static FetchOptions withDefaults() {
			FetchOptions fetchOptions = new FetchOptions ();
			fetchOptions.limit = DEFAULT_LIMIT;
			fetchOptions.offset = DEFAULT_OFFSET;
			return fetchOptions;
		}
		
		public static FetchOptions withLimit(int limit) {
			FetchOptions fetchOptions = withDefaults();
			fetchOptions.limit = limit;
			return fetchOptions;
		}
		
		public static FetchOptions withOffset(int offset) {
			FetchOptions fetchOptions = withDefaults();
			fetchOptions.offset = offset;
			return fetchOptions;
		}
	}
}
