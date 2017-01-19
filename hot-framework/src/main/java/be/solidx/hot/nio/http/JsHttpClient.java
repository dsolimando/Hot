package be.solidx.hot.nio.http;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.DocumentBuilder;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.springframework.http.MediaType;

import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.nio.http.HttpDataSerializer.HttpDataSerializationException;
import be.solidx.hot.nio.http.Request.Response;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.js.JSDeferred;

import com.google.common.net.HttpHeaders;

public class JsHttpClient extends HttpClient<NativeFunction, NativeObject> {

	final Scriptable globalScope;
	
	final DocumentBuilder documentBuilder;
	
	final JsMapConverter jsDataConverter;
	
	public JsHttpClient(
			ExecutorService eventLoop,
			NioClientSocketChannelFactory nioClientSocketChannelFactory, 
			SSLContextBuilder sslContextBuilder, 
			ObjectMapper objectMapper, 
			HttpDataSerializer httpDataSerializer,
			DocumentBuilder documentBuilder,
			Scriptable globalScope, 
			JsMapConverter jsDataConverter) {
		super(eventLoop, nioClientSocketChannelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
		this.globalScope = globalScope;
		this.documentBuilder = documentBuilder;
		this.jsDataConverter = jsDataConverter;
	}

	@Override
	public Request<NativeFunction, NativeObject> buildRequest(NativeObject options) throws SSLContextBuilder.SSLContextInitializationException {
		Request<NativeFunction, NativeObject> request = new JsRequest(
				options, 
				eventLoop,
				channelFactory, 
				sslContextBuilder, 
				objectMapper, 
				httpDataSerializer, 
				documentBuilder, 
				globalScope, 
				jsDataConverter);
		request.init(channelFactory);
		return request;
	}

	public static class JsRequest extends Request<NativeFunction, NativeObject> {

		final Scriptable 		globalScope;
		final JsMapConverter 	jsDataConverter;
		
		public JsRequest(Map<String, Object> options, 
				ExecutorService eventLoop,
				ChannelFactory channelFactory, 
				SSLContextBuilder sslContextBuilder, 
				ObjectMapper objectMapper, 
				HttpDataSerializer httpDataSerializer,
				DocumentBuilder documentBuilder,
				Scriptable globalScope, 
				JsMapConverter jsDataConverter) {
			super(options, eventLoop, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
			this.globalScope = globalScope;
			this.jsDataConverter = jsDataConverter;
			this.documentBuilder = documentBuilder;
		}

		@Override
		void executeSuccessClosure(Object data, String status, Request.Response<NativeFunction, NativeObject> response) {
			Context context = Context.enter();
			successClosure.call(context, globalScope, successClosure, new Object[]{data, status, response});
			Context.exit();
		}

		@Override
		void executeProgressClosure(Object data, String status, Request.Response<NativeFunction, NativeObject> response) {
			Context context = Context.enter();
			progressClosure.call(context, globalScope, successClosure, new Object[]{data, status, response});
			Context.exit();
		}

		@Override
		void executeErrorClosure(Response<NativeFunction, NativeObject> response, String status, Throwable errorMessage) {
			Context context = Context.enter();
			successClosure.call(context, globalScope, successClosure, new Object[]{response, status, new WrappedException(errorMessage)});
			Context.exit();
		}

		@Override
		protected Deferred<NativeFunction> buildDeferred() {
			return new JSDeferred(globalScope);
		}

		@Override
		protected Response<NativeFunction, NativeObject> buildResponse() {
			return new JsResponse(jsDataConverter);
		}

		@Override
		protected Exception buildFailException(Exception exception) {
			return new WrappedException(exception);
		}
		
		@Override
		Object fromJSON(byte[] json) throws JsonParseException, JsonMappingException, IOException {
			try {
				return jsDataConverter.toScriptMap(objectMapper.readValue(json, Map.class));
			} catch (Exception e) {
				List<Map<?, ?>> transformedList = new ArrayList<>();
				List<?> mappedList = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
				for (Object object : mappedList) {
					transformedList.add(jsDataConverter.toScriptMap((Map<?, ?>) object));
				}
				return new NativeArray(transformedList.toArray());
			}
		}
		
		@Override
		protected byte[] processRequestData() {
			return processRequestData(null);
		}
		
		@Override
		protected byte[] processRequestData(MediaType ct) {
			if (options.get(DATA) != null) {
				MediaType contentType = ct;
				if (contentType == null) contentType = requestContentType();
				try {
					return httpDataSerializer.serialize(options.get(DATA), contentType);
				} catch (HttpDataSerializationException e) {
					return null;
				}
			}
			return null;
		}
		
		@Override
		protected void addLoginPasswordBase64(String base64Data) {
			Object o = super.options.get(HEADERS);
			if (o != null) {
				NativeObject headers = (NativeObject)o;
				headers.put(HttpHeaders.AUTHORIZATION, headers, base64Data);
			} else {
				NativeObject headers = new NativeObject();
				headers.put(HttpHeaders.AUTHORIZATION,headers, base64Data);
				super.options.put(HEADERS, headers);
			}
		}
	}
	
	public static class JsResponse extends Response<NativeFunction, NativeObject> {
		
		final JsMapConverter jsDataConverter;
		
		public JsResponse(JsMapConverter jsDataConverter) {
			super();
			this.jsDataConverter = jsDataConverter;
		}

		@Override
		public NativeObject getHeaders() {
			return jsDataConverter.toScriptMap(headers);
		}
		
	}
}
