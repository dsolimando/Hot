package be.solidx.hot.spring.config;

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import be.solidx.hot.shows.AbstractWebSocket.Options;
import be.solidx.hot.shows.AbstractWebSocketHandler;
import be.solidx.hot.shows.Show;
import be.solidx.hot.spring.config.event.WebSocketActivationEvent;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer, ApplicationListener<WebSocketActivationEvent> {
	
	private static final Log LOG = LogFactory.getLog(WebSocketConfig.class);

	@Autowired
	ShowConfig showConfig;
	
	WebSocketHandlerRegistry registry;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		this.registry = registry;
		try {
			for (Show<?, ?> show : showConfig.showsContext().getShows()) {
				
				if (show.getWebsocket() != null && !show.getWebsocket().getSocketHandlerAdapterMap().isEmpty()) {
					for (Entry entry : show.getWebsocket().getSocketHandlerAdapterMap().entrySet()) {
						AbstractWebSocketHandler<?> handler = (AbstractWebSocketHandler<?>) entry.getValue();
						Options options = (Options) entry.getKey();
						registry.addHandler(handler.getSocketHandlerAdapter(), options.getPath());
					}
				}
			}
		} catch (Exception e) {
			LOG.error("",e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onApplicationEvent(WebSocketActivationEvent event) {
		for (Entry entry : event.getWebsocketHandlers().entrySet()) {
			AbstractWebSocketHandler<?> handler = (AbstractWebSocketHandler) entry.getValue();
			Options options = (Options) entry.getKey();
			registry.addHandler(handler.getSocketHandlerAdapter(), options.getPath());
		}
	}
}
