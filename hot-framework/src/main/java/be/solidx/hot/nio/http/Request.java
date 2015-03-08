package be.solidx.hot.nio.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.xml.parsers.DocumentBuilder;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;
import org.w3c.dom.Document;

import be.solidx.hot.nio.http.HttpDataSerializer.HttpDataSerializationException;
import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

import com.google.common.net.HttpHeaders;

public abstract class Request<CLOSURE,MAP> implements Promise<CLOSURE> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
	
	protected static final String TARGET_URL = "url";
	protected static final String TYPE = "type";
	protected static final String DATA = "data";
	protected static final String TIMEOUT = "timeout";
	protected static final String PROCESS_DATA = "processData";
	protected static final String PROCESS_REQUEST = "processRequest";
	protected static final String PROCESS_RESPONSE = "processResponse";
	protected static final String HEADERS = "headers";
	protected static final String SUCCESS = "success";
	protected static final String ERROR = "error";
	protected static final String PROGRESS = "pagress";
	protected static final String USERNAME = "username";
	protected static final String PASSWORD = "password";
	protected static final String SSL = "ssl";
	protected static final String CANCEL_MESSAGE 	= "Request to Server has been canceled";
	
	private static final String DEFAULT_RESPONSE_CONTENT_TYPE = "application/octet-stream";
	private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";
	private static final String DEFAULT_REQUEST_CONTENT_TYPE  = "text/plain;charset=utf-8";
	
	final SSLContextBuilder sslContextBuilder;
	final ObjectMapper 		objectMapper;
	final HttpDataSerializer httpDataSerializer;
	final ExecutorService 	eventLoop;
	
	ClientBootstrap 		clientBootstrap;
	
	Map<String, Object> options;
	
	CLOSURE successClosure;
	CLOSURE errorClosure;
	CLOSURE progressClosure;
	
	Deferred<CLOSURE> deferred;
	
	boolean ssl;
	
	DocumentBuilder documentBuilder;
	
	public Request(Map<String, Object> options, 
			ExecutorService eventLoop,
			ChannelFactory channelFactory, 
			SSLContextBuilder sslContextBuilder,
			ObjectMapper objectMapper,
			HttpDataSerializer httpDataSerializer) {
		
		loadOptions(options);
		this.objectMapper = objectMapper;
		this.sslContextBuilder = sslContextBuilder;
		this.httpDataSerializer = httpDataSerializer;
		this.eventLoop = eventLoop;
	}
	
	@Override
	public Promise<CLOSURE> done(CLOSURE closure) {
		deferred.done(closure);
		return this;
	}
	
	@Override
	public Promise<CLOSURE> fail(CLOSURE closure) {
		deferred.fail(closure);
		return this;
	}
	
	@Override
	public Promise<CLOSURE> always(CLOSURE closure) {
		deferred.always(closure);
		return this;
	}
	
	@Override
	public Promise<CLOSURE> progress(CLOSURE closure) {
		deferred.progress(closure);
		return this;
	}
	
	@Override
	public Promise<CLOSURE> then(CLOSURE doneClosure) {
		return deferred.then(doneClosure);
	}
	
	@Override
	public Promise<CLOSURE> then(CLOSURE doneClosure, CLOSURE failClosure) {
		return deferred.then(doneClosure, failClosure);
	}
	
	@Override
	public Promise<CLOSURE> then(CLOSURE doneClosure, CLOSURE failClosure, CLOSURE progressClosure) {
		return deferred.then(doneClosure, failClosure, progressClosure);
	}
	
	@Override
	public String state() {
		return deferred.state();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public org.jdeferred.Promise getPromise() {
		return deferred.getPromise();
	}
	
	@Override
	public Promise<CLOSURE> _done(be.solidx.hot.promises.Promise.DCallback callback) {
		return deferred._done(callback);
	}
	
	@Override
	public Promise<CLOSURE> _fail(be.solidx.hot.promises.Promise.FCallback callback) {
		return deferred._fail(callback);
	}
	
	protected void init (ChannelFactory channelFactory) {
		deferred = buildDeferred();
		clientBootstrap = new ClientBootstrap(channelFactory);
		clientBootstrap.setPipelineFactory(buildChannelPipelineFactory());
		
		final URL url = (URL) this.options.get(TARGET_URL);
		ChannelFuture channelFuture = clientBootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort()!=-1?url.getPort():80));
		
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
//				if (!channelFuture.isSuccess()) {
//					if (errorClosure != null) 
//						executeErrorClosure(null, "Connection Failed", channelFuture.getCause());
//					deferred.reject(null, "Request cancelled", CANCEL_MESSAGE);
//				} else 
					if (channelFuture.isSuccess()) {
					HttpRequest httpRequest = prepareRequest(url);
					channelFuture.getChannel().getConfig().setConnectTimeoutMillis((int) options.get(TIMEOUT));
					channelFuture.getChannel().write(httpRequest);
				}
//				} else if (channelFuture.isCancelled()) {
//					if (errorClosure != null) 
//						executeErrorClosure(null, "Request cancelled", channelFuture.getCause());
//					deferred.reject(null, "Request cancelled", CANCEL_MESSAGE);
//				}
			}
		});
	}

	private HttpRequest prepareRequest(URL url) {
		HttpRequest httpRequest = new DefaultHttpRequest(
				HttpVersion.HTTP_1_1, 
				(HttpMethod)options.get(TYPE), 
				url.getPath());
		
		// Set request http headers
		boolean hostnameHeader = false;
		if (Request.this.options.get(HEADERS) != null) {
			for (Entry<String, String> entry : ((Map<String, String>)Request.this.options.get(HEADERS)).entrySet()) {
				httpRequest.headers().set(entry.getKey(),entry.getValue());
				if (entry.getKey().equals("Host")) {
					hostnameHeader = true;
				}
			}
		}
		if (!hostnameHeader) {
			httpRequest.headers().set("Host", url.getHost());
		}
		
		// Set request payload
		if (options.get(DATA) != null) {
			Charset urlDataCharset = requestContentType().getCharSet();
			if (urlDataCharset == null)  urlDataCharset = Charset.forName("utf-8");
			if (httpRequest.getMethod().equals(HttpMethod.GET)) {
				httpRequest.setUri(httpRequest.getUri()+"?"+new String(processRequestData(MediaType.APPLICATION_FORM_URLENCODED), urlDataCharset));
			} else {
				if ((boolean) options.get(PROCESS_DATA)) {
					byte[] processedData = processRequestData();
					if (processedData != null)
						httpRequest.setContent(ChannelBuffers.wrappedBuffer(processedData));
						httpRequest.headers().set(HttpHeaders.CONTENT_LENGTH,processedData.length);
				} else {
					byte[] payload;
					if (options.get(DATA) instanceof String) {
						payload = ((String)options.get(DATA)).getBytes(urlDataCharset);
					} else {
						payload = (options.get(DATA).toString().getBytes(urlDataCharset));
					}
					httpRequest.headers().set(HttpHeaders.CONTENT_LENGTH,payload.length);
					httpRequest.setContent(ChannelBuffers.wrappedBuffer(payload));
				}
			}
		}
		return httpRequest;
	}
	
	private ChannelPipelineFactory buildChannelPipelineFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("log", new LoggingHandler());
				if (ssl) {
					SSLContext sslContext = sslContextBuilder.buildSSLContext((Map<String, Object>) (options.get(SSL) != null? options.get(SSL):new HashMap<>()));
					SSLEngine sslEngine = sslContext.createSSLEngine();
					sslEngine.setUseClientMode(true);
					pipeline.addLast("ssl", new SslHandler(sslEngine));
				}
				pipeline.addLast("codec", new HttpClientCodec());
				pipeline.addLast("inflater", new HttpContentDecompressor());
				pipeline.addLast("handler", new HotSimpleChannelUpstreamHandler());
				return pipeline;
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	private void loadOptions (Map<String, Object> options) {
		this.options = new HashMap<>();
//		this.options.putAll(options);
		if (options.get(TYPE) != null) {
			this.options.put(TYPE, HttpMethod.valueOf(options.get(TYPE).toString()));
		} else {
			this.options.put(TYPE, HttpMethod.GET);
		}
		try {
			if (options.get(TARGET_URL) != null) {
				URL url = new URL(options.get(TARGET_URL).toString());
				if (url.getProtocol().equals("https")) {
					ssl = true;
					if (url.getPort() == -1)
						url = new URL (url.getProtocol(),url.getHost(),443,url.getFile());
					this.options.put(SSL, options.get(SSL));
				} else if (!url.getProtocol().equals("http")) {
					throw new RuntimeException("Invalid protocol " + url.getProtocol()+". Only http and https are supported");
				}
				this.options.put(TARGET_URL, url);
			} else {
				this.options.put(TARGET_URL, new URL("http://localhost:80"));
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		if (options.get(HEADERS) != null && options.get(HEADERS) instanceof Map) {
			this.options.put(HEADERS, options.get(HEADERS));
		}
		
		if (options.get(USERNAME) != null) {
			String password = "";
			if (options.get(PASSWORD) != null) {
				password = options.get(PASSWORD).toString();
			}
			
			try {
				String base64Auth = "Basic "+ new String(Base64.encode(String.format("%s:%s", options.get(USERNAME), password).getBytes("UTF-8")));
				LOGGER.debug("Base64 auth: "+base64Auth);
				addLoginPasswordBase64(base64Auth);
				
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("",e);
			}
		}
		
		if (options.get(SUCCESS) != null) {
			successClosure = (CLOSURE) options.get(SUCCESS);
		}
		
		if (options.get(ERROR) != null) {
			errorClosure = (CLOSURE) options.get(ERROR);
		}
		
		if (options.get(PROGRESS) != null) {
			progressClosure = (CLOSURE) options.get(PROGRESS);
		}
		
		if (options.get(PROCESS_RESPONSE) != null) {
			this.options.put(PROCESS_RESPONSE, convertBoolean(options.get(PROCESS_RESPONSE)));
		} else {
			this.options.put(PROCESS_RESPONSE, true);
		}
		
		if (options.get(TIMEOUT) != null) {
			this.options.put(TIMEOUT, Integer.parseInt(options.get(TIMEOUT).toString()));
		} else {
			this.options.put(TIMEOUT,0);
		}
		
		if (options.get(DATA) != null) {
			if (options.get(PROCESS_DATA) != null) {
				this.options.put(PROCESS_DATA, convertBoolean(options.get(PROCESS_DATA)));
			} else if (options.get(PROCESS_REQUEST) != null) {
				this.options.put(PROCESS_DATA, convertBoolean(options.get(PROCESS_REQUEST)));
			} else {
				this.options.put(PROCESS_DATA, true);
			}
			this.options.put(DATA, options.get(DATA));
		}
	}
	
	protected abstract void addLoginPasswordBase64(String base64Data);
	
	private boolean convertBoolean (Object input) {
		if (input instanceof String) {
			return Boolean.parseBoolean((String) input);
		} else if (input instanceof Boolean) {
			return (boolean) input;
		} else if (input instanceof Integer) {
			Integer integer = (Integer) options.get(PROCESS_RESPONSE);
			if (integer > 0) {
				return true;
			} else {
				return false;
			}
		} else return false;
	}
	
	protected MediaType requestContentType () {
		String contentType = DEFAULT_REQUEST_CONTENT_TYPE;
		if (options.get(HEADERS) != null) {
			Map headers = (Map)options.get(HEADERS);
			for (Object key : headers.keySet()) {
				if (key instanceof String && ((String) key).toLowerCase().equals(HttpHeaders.CONTENT_TYPE.toLowerCase())) {
					contentType = (String) headers.get(key);
					break;
				}
			}
		}
		return MediaType.parseMediaType(contentType);
	}
	
	protected Object fromXML (byte[] xmlData) throws Exception {
		return documentBuilder.parse(new ByteArrayInputStream(xmlData));
	}
	
	protected byte[] processRequestData () {
		if (options.get(DATA) != null) {
			MediaType contentType = requestContentType();
			try {
				return httpDataSerializer.serialize(options.get(DATA), contentType);
			} catch (HttpDataSerializationException e) {
				return null;
			}
		}
		return null;
	}
	
	protected byte[] processRequestData (MediaType contentType) {
		if (options.get(DATA) != null) {
			try {
				return httpDataSerializer.serialize(options.get(DATA), contentType);
			} catch (HttpDataSerializationException e) {
				return null;
			}
		}
		return null;
	}
	
	abstract void executeSuccessClosure(Object data, String status, Response<CLOSURE, MAP> response);
	abstract void executeProgressClosure(Object data, String status, Response<CLOSURE, MAP> response);
	abstract void executeErrorClosure(Response<CLOSURE, MAP> response, String status, Throwable exception);
	
	abstract Exception buildFailException(Exception exception);
	abstract Object fromJSON (byte[] json) throws JsonParseException, JsonMappingException, IOException;
	
	protected abstract Deferred<CLOSURE> buildDeferred();
	protected abstract Response<CLOSURE, MAP> buildResponse ();
	
	protected class HotSimpleChannelUpstreamHandler extends SimpleChannelUpstreamHandler {

		boolean chunked;
		
		private Response<CLOSURE, MAP> response;
		
		private MediaType responseContentType;
		
		ByteArrayOutputStream chunkedBytes = new ByteArrayOutputStream();
		
		public HotSimpleChannelUpstreamHandler() {
			super();
			response = buildResponse();
		}
		
		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
//			super.exceptionCaught(ctx, e);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("",e);
			
			if (deferred.getPromise().isRejected()) 
				return;
			
			eventLoop.execute(new Runnable() {
				@Override
				public void run() {
					deferred.reject(e.getCause(), e.getCause().getMessage(), null);
					if (errorClosure != null) {
						executeErrorClosure(null, e.getCause().getMessage(), e.getCause());
					}
				}
			});
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
			if (!chunked) {
				
				final HttpResponse httpResponse = (HttpResponse) event.getMessage();
				mapResponseParameters(httpResponse);
				
				String contentType = httpResponse.headers().get(com.google.common.net.HttpHeaders.CONTENT_TYPE);
				if (contentType != null) {
					responseContentType = MediaType.parseMediaType(contentType);
				} else {
					responseContentType = MediaType.parseMediaType(DEFAULT_RESPONSE_CONTENT_TYPE);
				}
				response.contentType = responseContentType.getType()+"/"+responseContentType.getSubtype();
				response.encoding = responseContentType.getCharSet() == null?Charset.forName(DEFAULT_RESPONSE_ENCODING):responseContentType.getCharSet();
				
				if (httpResponse.isChunked()) {
					chunked = true;
				} else {
					byte[] data = httpResponse.getContent().array();
					final Object processedResponseData = ((Boolean)options.get(PROCESS_RESPONSE))?processResponseData(data):processChunkData(data);
					
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							if (successClosure != null) {
								executeSuccessClosure(processedResponseData, response.statusText, response);
							}
							deferred.resolve(processedResponseData, httpResponse.getStatus().getReasonPhrase(), response);
						}
					});
//					eventLoopPool.execute(new Runnable() {
//						@Override
//						public void run() {
//							clientBootstrap.releaseExternalResources();
//						}
//					});
				}
				Document document;
			} else {
				HttpChunk chunk = (HttpChunk)event.getMessage();
				if (chunk.isLast()) {
					final Object processedResponseData = processResponseData(chunkedBytes.toByteArray());
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							if (successClosure != null) {
								executeSuccessClosure(processedResponseData, response.statusText, response);
							}
							deferred.resolve(processedResponseData, response.statusText, response);
						}
					});
//					eventLoopPool.execute(new Runnable() {
//						@Override
//						public void run() {
//							clientBootstrap.releaseExternalResources();
//						}
//					});
				} else {
					byte[] receivedBytes = chunk.getContent().array();
					final Object processedChunk = processChunkData(receivedBytes);
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							if (progressClosure != null) {
								executeProgressClosure(processedChunk, response.statusText, response);
							}
							deferred.notify(processedChunk, response.statusText, response);
						}
					});
					chunkedBytes.write(receivedBytes);
				}
			}
		}
		
		private Object processResponseData (byte[] data) {
			if (response.contentType.equals(MediaType.APPLICATION_OCTET_STREAM.toString())) {
				return data;
			} else if (response.contentType.equals(MediaType.APPLICATION_JSON.toString())) {
				try {
					return fromJSON(data);
				} catch (IOException e) {
					LOGGER.error("Failed to convert JSON response",e);
					return new String(data,response.encoding);
				}
			} else if (response.contentType.equals(MediaType.APPLICATION_XML.toString())) {
				try {
					return fromXML(data);
				} catch (Exception e) {
					LOGGER.error("Failed to convert XML response",e);
					return new String(data,response.encoding);
				}
			} else {
				return new String(data,response.encoding);
			}
		}
		
		private Object processChunkData(byte[] data) {
			if (response.contentType.equals(MediaType.APPLICATION_OCTET_STREAM.toString())) {
				return data;
			} else {
				return new String(data,response.encoding);
			}
		}
		
		private void mapResponseParameters(HttpResponse httpResponse) {
			response.statusCode = httpResponse.getStatus().getCode();
			response.statusText = httpResponse.getStatus().getReasonPhrase();
			response.clearHeaders();
			for (Entry<String, String> header : httpResponse.headers().entries()) {
				response.headers.put(header.getKey(), header.getValue());
			}
		}
	}
	
	protected abstract static class Response<CLOSURE,MAP> {
		
		Charset encoding = Charset.forName("UTF-8");
		
		int statusCode = -1;
		
		String statusText;
		String contentType;
		
		protected Map<String, Object> headers; 
		
		public void setEncoding(String encoding) {
			this.encoding = Charset.forName(encoding);
		}
		
		public abstract MAP getHeaders();
		
		public int getStatusCode() {
			return statusCode;
		}
		
		public String getStatusText() {
			return statusText;
		}
		
		void clearHeaders() {
			if (headers == null) {
				headers = new HashMap<>();
			} else {
				headers.clear();
			}
		}
	}
	
	public static void main(String[] args) throws MalformedURLException {
		URL url = new URL("https://toto.com/sdsds?a=2&b=3");
		System.out.println(url.getPort());
	}
}