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
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.DocumentBuilder;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;

import be.solidx.hot.nio.http.HttpDataSerializer.HttpDataSerializationException;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.python.PythonDeferred;
import be.solidx.hot.python.PyDictionaryConverter;

public class PythonHttpClient extends HttpClient<PyFunction, PyDictionary> {

	private PyDictionaryConverter pyDataConverter;
	
	private DocumentBuilder documentBuilder;
	
	public PythonHttpClient(
			ExecutorService eventLoop,
			NioClientSocketChannelFactory nioClientSocketChannelFactory, 
			SSLContextBuilder sslContextBuilder, 
			ObjectMapper objectMapper, 
			HttpDataSerializer httpDataSerializer,
			DocumentBuilder documentBuilder,
			PyDictionaryConverter pyDataConverter) {
		super(eventLoop,nioClientSocketChannelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
		this.pyDataConverter = pyDataConverter;
		this.documentBuilder = documentBuilder;
	}

	@Override
	public Request<PyFunction, PyDictionary> buildRequest(PyDictionary options) {
		PyRequest request = new PyRequest(
				options,
				eventLoop,
				channelFactory, 
				sslContextBuilder, 
				objectMapper, 
				httpDataSerializer, 
				documentBuilder, 
				pyDataConverter);
		request.init(channelFactory);
		return request;
	}

	public static class PyRequest extends Request<PyFunction, PyDictionary> {
		
		PyDictionaryConverter pyDataConverter;

		public PyRequest(
				Map<String, Object> options, 
				ExecutorService eventLoop,
				ChannelFactory channelFactory, 
				SSLContextBuilder sslContextBuilder, 
				ObjectMapper objectMapper,
				HttpDataSerializer httpDataSerializer, 
				DocumentBuilder documentBuilder, 
				PyDictionaryConverter pyDataConverter) {
			
			super(options, eventLoop, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
			this.pyDataConverter = pyDataConverter;
			this.documentBuilder = documentBuilder;
		}

		@Override
		void executeSuccessClosure(Object data, String status, Response<PyFunction, PyDictionary> response) {
			successClosure.__call__(new PyObject[] {(PyObject) data, new PyString(status), Py.java2py(response) });
		}

		@Override
		void executeProgressClosure(Object data, String status, be.solidx.hot.nio.http.Request.Response<PyFunction, PyDictionary> response) {
			successClosure.__call__(new PyObject[] {(PyObject) data, new PyString(status), Py.java2py(response) });
		}

		@Override
		void executeErrorClosure(be.solidx.hot.nio.http.Request.Response<PyFunction, PyDictionary> response, String status, Throwable exception) {
			successClosure.__call__(new PyObject[] {Py.java2py(response), new PyString(status), Py.java2py(exception)  });
		}

		@Override
		Exception buildFailException(Exception exception) {
			return Py.JavaError(exception);
		}

		@Override
		PyDictionary fromJSON(byte[] json) throws JsonParseException, JsonMappingException, IOException {
			return pyDataConverter.toScriptMap(objectMapper.readValue(json, Map.class));
		}

		@Override
		protected Deferred<PyFunction> buildDeferred() {
			return new PythonDeferred();
		}

		@Override
		protected Response<PyFunction, PyDictionary> buildResponse() {
			return new PyResponse(pyDataConverter);
		}
		
		@Override
		protected byte[] processRequestData(MediaType ct) {
			if (options.get(DATA) != null) {
				MediaType contentType = ct;
				if (contentType == null) contentType = requestContentType();
				try {
					return httpDataSerializer.serialize(pyDataConverter.toMap((PyDictionary) options.get(DATA)), contentType);
				} catch (HttpDataSerializationException e) {
					return null;
				}
			}
			return null;
		}
		
		@Override
		protected byte[] processRequestData() {
			return processRequestData(null);
		}
		
		@Override
		protected void addLoginPasswordBase64(String base64Data) {
			Object o = super.options.get(HEADERS);
			if (o != null) {
				PyDictionary headers = (PyDictionary) o;
				headers.put(HttpHeaders.AUTHORIZATION, base64Data);
			} else {
				PyDictionary headers = new PyDictionary();
				headers.put(HttpHeaders.AUTHORIZATION, base64Data);
				super.options.put(HEADERS, headers);
			}
		}
		
		public static class PyResponse extends Response<PyFunction, PyDictionary>{

			PyDictionaryConverter pyDataConverter;
			
			public PyResponse(PyDictionaryConverter pyDataConverter) {
				super();
				this.pyDataConverter = pyDataConverter;
			}

			@Override
			public PyDictionary getHeaders() {
				return pyDataConverter.toScriptMap(headers);
			}
			
		}
	}
}
