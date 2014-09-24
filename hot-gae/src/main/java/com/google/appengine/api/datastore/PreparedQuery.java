package com.google.appengine.api.datastore;

import java.util.List;

public interface PreparedQuery {
	Iterable<Entity> asIterable();
	Iterable<Entity> asIterable(FetchOptions fetchOptions);
	List<Entity> asList(FetchOptions fetchOptions);
	Entity asSingleEntity();
	int countEntities(FetchOptions fetchOptions);
	
	@SuppressWarnings("serial")
	public static class TooManyResultsException  extends RuntimeException {
	}
}
