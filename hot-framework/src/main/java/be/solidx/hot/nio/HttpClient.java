package be.solidx.hot.nio;

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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;

public abstract class HttpClient<CLOSURE, MAP> {
	
	private static final String HOSTNAME = "hostname";
	private static final String PORT = "port";
	private static final String PATH = "path";
	private static final String METHOD = "method";
	private static final String HEADERS = "headers";
	
	ExecutorService eventLoopPool;
	
	DefaultConversionService conversionService;
	
	OptionsMapper headersMapper = buildOptionsMapper();
	NioClientSocketChannelFactory channelFactory;
	
	@Autowired
	public HttpClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		//InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
		this.eventLoopPool = eventLoopPool;
		this.conversionService = defaultConversionService;
		this.channelFactory = new NioClientSocketChannelFactory(bossExecutorService, eventLoopPool, 1, 1);
	}
	
	@Autowired
	public HttpClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		//InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
		this.eventLoopPool = eventLoopPool;
		this.conversionService = defaultConversionService;
		this.channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), eventLoopPool, 1, 1);
	}

	public Request request (MAP options, CLOSURE requestClosure) {
		return buildRequest(options,requestClosure);
	}
	
	protected OptionsMapper buildOptionsMapper() {
		return new OptionsMapper();
	}
	
	protected abstract Request buildRequest (MAP options, CLOSURE requestClosure);
	
	protected abstract Response<CLOSURE, MAP> buildResponse ();
	
	protected abstract class Request {
		
		private static final String ERROR_EVENT = "error";
		private static final String CANCELED_EVENT = "canceled";
		
		private static final String CANCEL_MESSAGE = "Request to Server has been canceled";
		
		ClientBootstrap clientBootstrap;
		
		Map<String, Object> options;
		
		RequestClosure<CLOSURE,MAP> requestClosure;
		
		RequestErrorClosure<CLOSURE> requestErrorClosure;
		
		RequestCanceledClosure<CLOSURE> requestCanceledClosure;
		
		Channel channel;
		HttpRequest request;
		protected Response<CLOSURE, MAP> response;
		private boolean end;
		
		public Request(Map<String, Object> options, CLOSURE requestClosure) {
			
			this.options = headersMapper.toNettyOptions(options);
			this.requestClosure = buildRequestClosure(requestClosure);
			
			clientBootstrap = new ClientBootstrap(channelFactory);
			response = buildResponse();
			clientBootstrap.setPipelineFactory(buildChannelPipelineFactory());
			
			ChannelFuture channelFuture = clientBootstrap.connect(new InetSocketAddress((String)this.options.get(HOSTNAME), (Integer)this.options.get(PORT)));
			
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (!channelFuture.isSuccess()) {
						if (requestErrorClosure != null)
							requestErrorClosure.call(channelFuture.getCause().getMessage());
					} else if (channelFuture.isSuccess()) {
						onSuccess(channelFuture.getChannel());
					} else if (channelFuture.isCancelled()) {
						if (requestCanceledClosure != null) 
							requestCanceledClosure.call(CANCEL_MESSAGE);
					}
				}
			});
		}

		public void on (String event, CLOSURE closure) {
			switch (event) {
			case ERROR_EVENT:
				requestErrorClosure = buildRequestErrorClosure(closure);
				break;
			
			case CANCELED_EVENT:
				requestCanceledClosure = buildRequestCanceledClosure(closure);
				break;
				
			default:
				break;
			}
		}
		
		public void end() {
			if (channel != null && request != null) {
				channel.write(request);
			} else {
				end = true;
			}
		}
		
		public void write (String chunck) throws UnsupportedEncodingException {
			if (request != null) {
				request.getContent().writeBytes(chunck.getBytes("utf-8"));
			}
		}
		
		@SuppressWarnings("unchecked")
		protected void onSuccess(Channel channel) {
			this.channel = channel;
			requestClosure.call(response);
			HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_0, (HttpMethod)options.get(METHOD), options.get(PATH).toString());
			if (options.get(HEADERS) != null) {
				for (Entry<String, String> entry : ((Map<String, String>)options.get(HEADERS)).entrySet()) {
					httpRequest.headers().set(entry.getKey(),entry.getValue());
				}
			}
			this.request = httpRequest;
			// End called before connection established
			if (end) {
				end();
			}
		}

		protected ChannelPipelineFactory buildChannelPipelineFactory() {
				return new ChannelPipelineFactory() {
				
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					ChannelPipeline pipeline = Channels.pipeline();
					pipeline.addLast("log", new LoggingHandler());
					pipeline.addLast("codec", new HttpClientCodec());
					pipeline.addLast("inflater", new HttpContentDecompressor());
					pipeline.addLast("handler", new HotSimpleChannelUpstreamHandler(response));
					return pipeline;
				}
			};
		}
		
		protected abstract RequestClosure<CLOSURE,MAP> buildRequestClosure (CLOSURE requestClosure);
		protected abstract RequestErrorClosure<CLOSURE> buildRequestErrorClosure (CLOSURE requestErrorClosure);
		protected abstract RequestCanceledClosure<CLOSURE> buildRequestCanceledClosure (CLOSURE requestCanceledClosure);
	}
	
	protected class HotSimpleChannelUpstreamHandler extends SimpleChannelUpstreamHandler {

		boolean chunked;
		
		Response<CLOSURE,MAP> response;
		
		public HotSimpleChannelUpstreamHandler(Response<CLOSURE,MAP> response) {
			super();
			this.response = response;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
			if (!chunked) {
				
				HttpResponse httpResponse = (HttpResponse) event.getMessage();
				mapResponseParameters(httpResponse);
				if (httpResponse.isChunked()) {
					chunked = true;
				} else {
					if (response != null && response.responseClosure != null) {
						response.responseClosure.call(httpResponse.getContent().toString(response.encoding));
						response.responseEndClosure.call();
					}
//					eventLoopPool.execute(new Runnable() {
//						@Override
//						public void run() {
//							clientBootstrap.releaseExternalResources();
//						}
//					});
				}
			} else {
				HttpChunk chunk = (HttpChunk)event.getMessage();
				if (chunk.isLast()) {
					if (response != null && response.responseEndClosure != null) {
						response.responseEndClosure.call();
					}
//					eventLoopPool.execute(new Runnable() {
//						@Override
//						public void run() {
//							clientBootstrap.releaseExternalResources();
//						}
//					});
				} else {
					if (response != null && response.responseClosure != null) {
						response.responseClosure.call(chunk.getContent().toString(response.encoding));
					}
				}
			}
		}
		
		private void mapResponseParameters(HttpResponse httpResponse) {
			response.statusCode = httpResponse.getStatus().getCode();
			response.clearHeaders();
			for (Entry<String, String> header : httpResponse.headers().entries()) {
				response.headers.put(header.getKey(), header.getValue());
			}
		}
	}
	
	public static interface RequestClosure<CLOSURE, MAP> {
		void call (Response<CLOSURE,MAP> response);
	}
	
	public static interface RequestErrorClosure<CLOSURE> {
		void call (String message);
	}
	
	public static interface RequestCanceledClosure<CLOSURE> {
		void call (String message);
	}
	
	public static interface ResponseClosure<CLOSURE> {
		void call (String chunck);
	}
	
	public static interface ResponseEndClosure<CLOSURE> {
		void call ();
	}
	
	public class OptionsMapper {
		
		Map<String, Object> toNettyOptions (Map<String, Object> options) {
			Map<String, Object> nettyOptions = new HashMap<>();
			if (options.get(METHOD) != null) {
				nettyOptions.put(METHOD, HttpMethod.valueOf(options.get(METHOD).toString()));
			} else {
				nettyOptions.put(METHOD, HttpMethod.GET);
			}
			if (options.get(HOSTNAME) != null) {
				nettyOptions.put(HOSTNAME, options.get(HOSTNAME).toString());
			} else {
				nettyOptions.put(HOSTNAME, "localhost");
			}
			if (options.get(PORT) != null) {
				nettyOptions.put(PORT, conversionService.convert(options.get(PORT), Integer.class));
			} else {
				nettyOptions.put(PORT, new Integer(80));
			}
			if (options.get(PATH) != null) {
				nettyOptions.put(PATH, options.get(PATH).toString());
			} else {
				nettyOptions.put(PATH, "/");
			}
			if (options.get(HEADERS) != null) {
				nettyOptions.put(HEADERS, options.get(HEADERS));
			}
			return nettyOptions;
		}
	}
	
	protected abstract static class Response<CLOSURE,MAP> {
		
		public static final String END = "end";
		public static final String DATA = "data";
		
		Charset encoding = Charset.forName("UTF-8");
		
		int statusCode = -1;
		
		protected Map<String, Object> headers; 
		
		ResponseClosure<CLOSURE> responseClosure;
		ResponseEndClosure<CLOSURE> responseEndClosure;
		
		public void on (String event, CLOSURE closure) {
			switch (event) {
			case DATA:
				responseClosure = buildResponseClosure(closure);
				break;

			case END:
				responseEndClosure = buildResponseEndClosure(closure);
				break;
				
			default:
				break;
			}
		}
		
		public void setEncoding(String encoding) {
			this.encoding = Charset.forName(encoding);
		}
		
		public abstract MAP getHeaders();
		
		public int getStatusCode() {
			return statusCode;
		}
		
		void clearHeaders() {
			if (headers == null) {
				headers = new HashMap<>();
			} else {
				headers.clear();
			}
		}
		
		protected abstract ResponseClosure<CLOSURE> buildResponseClosure(CLOSURE closure);
		protected abstract ResponseEndClosure<CLOSURE> buildResponseEndClosure(CLOSURE closure);
	}
	
	protected static class SSLContextInitializationException extends Exception {

		private static final long serialVersionUID = -792537019282300095L;

		public SSLContextInitializationException(Throwable cause) {
			super(cause);
		}
	}
	
	public static void main(String[] args) throws URISyntaxException, MalformedURLException, IOException {
		System.out.println(Runtime.getRuntime().availableProcessors());
		URI uri = new URI("http://localhost:8080/rest/names");
		System.out.println(uri.getRawPath());
		System.out.println(uri.getScheme());
		//System.out.println(IOUtils.toString((InputStream) new URL("http://hc.apache.org/httpcomponents-core-ga/tutorial/html/nio.html#d5e1072").getContent()));
	}
}
