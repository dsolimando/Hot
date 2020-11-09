package be.solidx.hot.shows;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import be.solidx.hot.Closure;
import be.solidx.hot.shows.WebSocket.Connection;
import be.solidx.hot.shows.WebSocket.Handler;

public abstract class AbstractWebSocketHandler<CLOSURE> implements Handler<CLOSURE> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebSocketHandler.class);
	
	ScriptTextWebSocketHandlerAdapter socketHandlerAdapter;
	
	ExecutorService eventLoop;
	
	boolean closed;
	
	public AbstractWebSocketHandler(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
		this.socketHandlerAdapter = new ScriptTextWebSocketHandlerAdapter();
	}

	@Override
	public Connection<CLOSURE> connect(CLOSURE onConnect) {
		AbstractConnection newConnection = new AbstractConnection();
		this.socketHandlerAdapter.setAbstractConnection(newConnection);
		socketHandlerAdapter.setConnectClosure(buildClosure(onConnect));
		return newConnection;
	}
	
	abstract protected Closure buildClosure(CLOSURE closure);
	
	public ScriptTextWebSocketHandlerAdapter getSocketHandlerAdapter() {
		return socketHandlerAdapter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof AbstractWebSocketHandler) {
			AbstractWebSocketHandler<CLOSURE> handler = (AbstractWebSocketHandler<CLOSURE>) obj;
			return handler.socketHandlerAdapter.equals(socketHandlerAdapter);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return socketHandlerAdapter.hashCode();
	}
	
	public class AbstractConnection implements Connection<CLOSURE> {

		Closure messageClosure;
		Closure closeClosure;
		
		WebSocketSession webSocketSession;
		
		@Override
		public void close(CLOSURE onClose) {
			closeClosure = buildClosure(onClose);
		}

		@Override
		public void data(CLOSURE onMessage) {
			messageClosure = buildClosure(onMessage);
		}

		@Override
		public void write(final String message) throws Exception {
			
			if (closed) webSocketSession.close();
			
			eventLoop.submit(new Runnable() {
				@Override
				public void run() {
					try {
						webSocketSession.sendMessage(new TextMessage(message));
					} catch (IOException e) {
						LOGGER.error("",e);
					}
				}
			});
		}
		
		public void setWebSocketSession(WebSocketSession webSocketSession) {
			this.webSocketSession = webSocketSession;
		}
	}

	public class ScriptTextWebSocketHandlerAdapter extends TextWebSocketHandler {
		
		Closure connectClosure;
		
		AbstractConnection abstractConnection;
		
		@Override
		public void afterConnectionEstablished(WebSocketSession session) throws Exception {
			
			if (closed) session.close();
			
			super.afterConnectionEstablished(session);
			if (connectClosure != null) {
				abstractConnection.setWebSocketSession(session);
				eventLoop.submit(new Runnable() {
					@Override
					public void run() {
						connectClosure.call(abstractConnection);
					}
				});
			}
		}
		
		@Override
		public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
			super.afterConnectionClosed(session, status);
			if (abstractConnection != null) {
				eventLoop.submit(new Runnable() {
					@Override
					public void run() {
						abstractConnection.closeClosure.call();
					}
				});
			}
		}
		
		@Override
		protected void handleTextMessage(WebSocketSession session, final TextMessage message) throws Exception {
			if (closed) session.close();
			eventLoop.submit(new Runnable() {
				@Override
				public void run() {
					abstractConnection.messageClosure.call(message.getPayload());
				}
			});
		}
		
		public void setConnectClosure(Closure connectClosure) {
			this.connectClosure = connectClosure;
		}
		
		public void setAbstractConnection(AbstractConnection abstractConnection) {
			this.abstractConnection = abstractConnection;
		}
		
		public void updateClosures (Closure connectClosure, Closure messageClosure, Closure closeClosure) {
			this.connectClosure = connectClosure;
			this.abstractConnection.closeClosure = closeClosure;
			this.abstractConnection.messageClosure = messageClosure;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof AbstractWebSocketHandler.ScriptTextWebSocketHandlerAdapter) {
				ScriptTextWebSocketHandlerAdapter adapter = (ScriptTextWebSocketHandlerAdapter) obj;
				return connectClosure.equals(adapter.connectClosure)
						&& abstractConnection.equals(adapter.abstractConnection);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return connectClosure.hashCode() + abstractConnection.hashCode();
		}
	}
}
