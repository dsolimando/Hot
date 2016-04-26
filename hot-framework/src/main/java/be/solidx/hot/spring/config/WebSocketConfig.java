package be.solidx.hot.spring.config;

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
