package be.icode.hot.nio.http;

import groovy.lang.Closure;
import groovy.util.XmlSlurper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.xml.sax.SAXException;

import be.icode.hot.nio.http.Request.Response;
import be.icode.hot.promises.Deferred;
import be.icode.hot.promises.groovy.GroovyDeferred;

public class GroovyHttpClient extends HttpClient<Closure<?>, Map<String, Object>> {

	XmlSlurper xmlSlurper;
	
	public GroovyHttpClient(NioClientSocketChannelFactory channelFactory, SSLContextBuilder sslContextBuilder, ObjectMapper objectMapper, HttpDataSerializer httpDataSerializer)  {
		super(channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
		try {
			xmlSlurper = new XmlSlurper();
		} catch (ParserConfigurationException | SAXException e) {}
	}

	@Override
	public Request<Closure<?>, Map<String, Object>> buildRequest(Map<String, Object> options) {
		Request<Closure<?>, Map<String, Object>> request = new GroovyRequest((Map<String, Object>) options, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer, xmlSlurper);
		request.init(channelFactory);
		return request;
	}
	
	public static class GroovyRequest extends Request<Closure<?>, Map<String, Object>> {

		XmlSlurper xmlSlurper;
		
		public GroovyRequest(Map<String, Object> options, 
				ChannelFactory channelFactory, 
				SSLContextBuilder sslContextBuilder, 
				ObjectMapper objectMapper,
				HttpDataSerializer httpDataSerializer,
				XmlSlurper xmlSlurper) {
			super(options, channelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
			this.xmlSlurper = xmlSlurper;
		}

		@Override
		void executeSuccessClosure(Object data, String status, be.icode.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response) {
			successClosure.call(data, status, response);
		}

		@Override
		void executeProgressClosure(Object data, String status, be.icode.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response) {
			progressClosure.call(data, status, response);
		}

		@Override
		void executeErrorClosure(be.icode.hot.nio.http.Request.Response<Closure<?>, Map<String, Object>> response, String status, Throwable errorMessage) {
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
		Object fromJSON(byte[] json) throws JsonParseException, JsonMappingException, IOException {
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
	}
	
	public static class GroovyResponse extends Response<Closure<?>, Map<String, Object>> {
		@Override
		public Map<String, Object> getHeaders() {
			return headers;
		}
	}
}
