package be.icode.hot.spring.config.event;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

import be.icode.hot.shows.AbstractWebSocket.Options;
import be.icode.hot.shows.WebSocket.Handler;

public class WebSocketActivationEvent extends ApplicationEvent {

	private static final long serialVersionUID = -9005403877960164148L;

	Map<Options, Handler<?>> websocketHandlers;
	
	public WebSocketActivationEvent(Object source, Map<Options, Handler<?>> websocketHandlers) {
		super(source);
		this.websocketHandlers = websocketHandlers;
	}

	public Map<Options, Handler<?>> getWebsocketHandlers() {
		return websocketHandlers;
	}
}
