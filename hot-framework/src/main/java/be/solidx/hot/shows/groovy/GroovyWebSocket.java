package be.solidx.hot.shows.groovy;

import groovy.lang.Closure;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.shows.AbstractWebSocket;
import be.solidx.hot.shows.AbstractWebSocketHandler;
import be.solidx.hot.shows.WebSocket;

public class GroovyWebSocket extends AbstractWebSocket<Closure<?>, Map<String,Object>> {

	public GroovyWebSocket(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected AbstractWebSocketHandler<Closure<?>> buildHandler(ExecutorService eventLoop) {
		return new GroovyHandler(eventLoop);
	}	
	
	@Override
	protected be.solidx.hot.shows.AbstractWebSocket.Options buildOptions(Map<String, Object> options) {
		return new Options((String) options.get(WebSocket.OPTION_PATH));
	}
	
	public class GroovyHandler extends AbstractWebSocketHandler<Closure<?>> {

		public GroovyHandler(ExecutorService eventLoop) {
			super(eventLoop);
		}
		
		@Override
		protected be.solidx.hot.Closure buildClosure(Closure<?> closure) {
			return new GroovyClosure(closure);
		}
	}
}
