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
import groovy.lang.Closure;
import groovy.util.XmlSlurper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.ParserConfigurationException;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.xml.sax.SAXException;

import com.google.common.net.HttpHeaders;

import be.solidx.hot.nio.http.Request.Response;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.groovy.GroovyDeferred;

public class GroovyHttpClient extends HttpClient<Closure<?>, Map<String, Object>> {

	XmlSlurper xmlSlurper;
	
	public GroovyHttpClient(
			ExecutorService eventLoop,
			NioClientSocketChannelFactory channelFactory, 
			SSLContextBuilder sslContextBuilder, 
			ObjectMapper objectMapper,
			HttpDataSerializer httpDataSerializer)  {
		super(eventLoop, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
		try {
			xmlSlurper = new XmlSlurper();
		} catch (ParserConfigurationException | SAXException e) {}
	}

	@Override
	public Request<Closure<?>, Map<String, Object>> buildRequest(Map<String, Object> options) throws SSLContextBuilder.SSLContextInitializationException {
		Request<Closure<?>, Map<String, Object>> request = new GroovyRequest(
				(Map<String, Object>) options,
				eventLoop,
				channelFactory, 
				sslContextBuilder, 
				objectMapper, 
				httpDataSerializer, 
				xmlSlurper);
		request.init(channelFactory);
		return request;
	}
	
	public static class GroovyRequest extends Request<Closure<?>, Map<String, Object>> {

		XmlSlurper xmlSlurper;
		
		public GroovyRequest(Map<String, Object> options, 
				ExecutorService eventLoop,
				ChannelFactory channelFactory, 
				SSLContextBuilder sslContextBuilder, 
				ObjectMapper objectMapper,
				HttpDataSerializer httpDataSerializer,
				XmlSlurper xmlSlurper) {
			super(options, eventLoop, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
			this.xmlSlurper = xmlSlurper;
		}

		@Override
		void executeSuccessClosure(Object data, String status, be.solidx.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response) {
			successClosure.call(data, status, response);
		}

		@Override
		void executeProgressClosure(Object data, String status, be.solidx.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response) {
			progressClosure.call(data, status, response);
		}

		@Override
		void executeErrorClosure(be.solidx.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response, String status, Throwable errorMessage) {
			errorClosure.call(response, status, errorMessage);
		}

		@Override
		protected Deferred<Closure<?>> buildDeferred() {
			return new GroovyDeferred();
		}

		@Override
		protected Response<Closure<?>, Map<String, Object>> buildResponse() {
			return new GroovyResponse();
		}

		@Override
		Exception buildFailException(Exception exception) {
			return exception;
		}

		@Override
		Object fromJSON(byte[] json) throws IOException {
			try {
				return objectMapper.readValue(json, Map.class);
			} catch (Exception e) {
				return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
			}
		}
		
		@Override
		protected Object fromXML(byte[] xmlData) throws Exception {
			return xmlSlurper.parse(new ByteArrayInputStream(xmlData));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void addLoginPasswordBase64(String base64Data) {
			Object o = super.options.get(HEADERS);
			if (o != null) {
				Map<String, Object> headers = (Map<String, Object>) o;
				headers.put(HttpHeaders.AUTHORIZATION, base64Data);
			} else {
				Map<String, Object> headers = new HashMap<>();
				headers.put(HttpHeaders.AUTHORIZATION, base64Data);
				super.options.put(HEADERS, headers);
			}
		}
	}
	
	public static class GroovyResponse extends Response<Closure<?>, Map<String, Object>> {
		@Override
		public Map<String, Object> getHeaders() {
			return headers;
		}
	}
}
