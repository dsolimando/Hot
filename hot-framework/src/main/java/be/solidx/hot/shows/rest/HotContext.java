package be.solidx.hot.shows.rest;

import be.solidx.hot.shows.ClosureRequestMapping;

public class HotContext {

	private static final ThreadLocal<ClosureRequestMapping> requestMapping = new ThreadLocal<>();
	
	public static void setRequestMapping (ClosureRequestMapping closureRequestMapping) {
		requestMapping.set(closureRequestMapping);
	}
	
	public static ClosureRequestMapping getRequestMapping () {
		return requestMapping.get();
	}
}
