package be.icode.hot.nio.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.DocumentBuilder;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.springframework.http.MediaType;

import be.icode.hot.js.JsMapConverter;
import be.icode.hot.nio.http.HttpDataSerializer.HttpDataSerializationException;
import be.icode.hot.nio.http.Request.Response;
import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.js.JSDeferred;

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
	public Request<NativeFunction, NativeObject> buildRequest(NativeObject options) {
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
		NativeObject fromJSON(byte[] json) throws JsonParseException, JsonMappingException, IOException {
			return jsDataConverter.toScriptMap(objectMapper.readValue(json, Map.class));
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
