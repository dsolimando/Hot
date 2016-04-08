package be.solidx.hot.shows;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
