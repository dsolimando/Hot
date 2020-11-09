package be.solidx.hot.nio.http;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;


public abstract class HttpClient<CLOSURE, MAP> {

	ExecutorService 				eventLoop;
	NioClientSocketChannelFactory 	channelFactory;
	SSLContextBuilder				sslContextBuilder;
	ObjectMapper objectMapper;
	HttpDataSerializer				httpDataSerializer;
	
	@Autowired
	public HttpClient(
			ExecutorService eventLoop,
			NioClientSocketChannelFactory channelFactory, 
			SSLContextBuilder sslContextBuilder, 
			ObjectMapper objectMapper, 
			HttpDataSerializer httpDataSerializer) {
		
		this.eventLoop = eventLoop;
		this.channelFactory = channelFactory;
		this.sslContextBuilder = sslContextBuilder;
		this.objectMapper = objectMapper;
		this.httpDataSerializer = httpDataSerializer;
	}

	public abstract Request<CLOSURE, MAP> buildRequest(MAP options) throws SSLContextBuilder.SSLContextInitializationException;
}
