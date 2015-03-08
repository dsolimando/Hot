package be.solidx.hot.shows;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;


public abstract class AbstractWebSocket<CLOSURE, MAP> implements WebSocket<CLOSURE, MAP> {
	
	Map<Options,Handler<CLOSURE>> socketHandlerAdapterMap = new HashMap<>();
	
	protected ExecutorService eventLoop;
	
	public AbstractWebSocket(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
	}

	@Override
	public AbstractWebSocketHandler<CLOSURE> addHandler(MAP options) {
		AbstractWebSocketHandler<CLOSURE> handler = buildHandler(eventLoop);
		socketHandlerAdapterMap.put(buildOptions(options), handler);
		return handler;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Options, Handler<CLOSURE>> refreshSocket(WebSocket<CLOSURE, MAP> updatedWebSocket) {
		MapDifference<Options, Handler<CLOSURE>> diff = Maps.difference(socketHandlerAdapterMap, updatedWebSocket.getSocketHandlerAdapterMap());
		for (Entry<Options, ValueDifference<Handler<CLOSURE>>> differingEntry : diff.entriesDiffering().entrySet()) {
			AbstractWebSocketHandler<CLOSURE> newHandler = (AbstractWebSocketHandler) differingEntry.getValue().rightValue();
			AbstractWebSocketHandler<CLOSURE> currentHandler = (AbstractWebSocketHandler) differingEntry.getValue().leftValue();
			currentHandler.getSocketHandlerAdapter().updateClosures(
					newHandler.socketHandlerAdapter.connectClosure, 
					newHandler.socketHandlerAdapter.abstractConnection.messageClosure, 
					newHandler.socketHandlerAdapter.abstractConnection.closeClosure);
		}
		for (Entry<Options, Handler<CLOSURE>> entry : diff.entriesOnlyOnLeft().entrySet()) {
			AbstractWebSocketHandler<CLOSURE> currentHandler = (AbstractWebSocketHandler) entry.getValue();
			currentHandler.closed = true;
		}
		socketHandlerAdapterMap.putAll(diff.entriesOnlyOnRight());
		return diff.entriesOnlyOnRight();
	}
	
	@Override
	public Map<Options, Handler<CLOSURE>> getSocketHandlerAdapterMap() {
		return socketHandlerAdapterMap;
	}
	
	abstract protected Options buildOptions (MAP options);
	
	abstract protected AbstractWebSocketHandler<CLOSURE> buildHandler(ExecutorService eventLoop);

	public static class Options {
		
		private String path;

		public Options(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof Options) {
				return path.equals(((Options)obj).path);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return path.hashCode();
		}
	}

	public void close() {
		for (Entry<Options, Handler<CLOSURE>> entry : socketHandlerAdapterMap.entrySet()) {
			((AbstractWebSocketHandler<CLOSURE>)entry.getValue()).closed = true;
		}
	}
}
