package be.solidx.hot.shows.rest;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.web.context.support.WebApplicationContextUtils;

import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.nio.http.HttpDataSerializer;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.Promise.DCallback;
import be.solidx.hot.promises.Promise.FCallback;
import be.solidx.hot.python.PyDictionaryConverter;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.shows.ClosureRequestMapping;
import be.solidx.hot.shows.RestRequest;
import be.solidx.hot.shows.groovy.GroovyRestRequest;
import be.solidx.hot.shows.javascript.JSRestRequest;
import be.solidx.hot.shows.python.PythonRestRequest;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.utils.GroovyHttpDataDeserializer;
import be.solidx.hot.utils.IOUtils;
import be.solidx.hot.utils.JsHttpDataDeserializer;
import be.solidx.hot.utils.PythonHttpDataDeserializer;



import hot.Response;

public class RestClosureServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestClosureServlet.class);
	
	private static final long serialVersionUID = 1534320093731293327L;
	
	protected static final String DEFAULT_CHARSET = "utf-8";
	protected static final String DEFAULT_ACCEPT = MediaType.TEXT_PLAIN.toString();

	ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;
	
	HttpDataSerializer 					httpDataSerializer;
	
	GroovyMapConverter 					groovyDataConverter;
	
	PyDictionaryConverter 				pyDictionaryConverter;
	
	JsMapConverter 						jsDataConverter;
	
	GroovyHttpDataDeserializer 			groovyHttpDataDeserializer;
	
	PythonHttpDataDeserializer 			pythonHttpDataDeserializer;
	
	JsHttpDataDeserializer 				jsHttpDataDeserializer;
	
	ExecutorService						blockingTreadPool;
	
	ExecutorService						httpIOEventLoop;
	
	@Override
	synchronized protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initSpringBeans();
		asyncHandleRestRequest(req, resp, req.startAsync());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	private void asyncHandleRestRequest (final HttpServletRequest req, final HttpServletResponse resp, final AsyncContext async) {
		
		try {
			// Parse accept media types header
			String acceptMediaTypeAsString = req.getHeader(com.google.common.net.HttpHeaders.ACCEPT);
			
			// Parse accept encoding header
			Enumeration<String> acceptEncodings = req.getHeaders(com.google.common.net.HttpHeaders.ACCEPT_ENCODING);
			String charsetAsString = null;
			
			if (acceptMediaTypeAsString == null) {
				acceptMediaTypeAsString = DEFAULT_ACCEPT;
			}
			List<MediaType> acceptMediaTypes = MediaType.parseMediaTypes(acceptMediaTypeAsString);
			MediaType selectedMediatype = acceptMediaTypes.get(0);
			
			if (acceptEncodings.hasMoreElements()) {
				charsetAsString = acceptEncodings.nextElement();
				try {
					if (!Charset.isSupported(charsetAsString)) {
						charsetAsString = DEFAULT_CHARSET;
					}
				} catch (IllegalCharsetNameException e) {
					charsetAsString = DEFAULT_CHARSET;
				} 
			} else {
				charsetAsString = DEFAULT_CHARSET;
			}
			// We construct the final mediatype with encoding from request
			final MediaType finalMediaType = new MediaType(selectedMediatype.getType(), selectedMediatype.getSubtype(), Charset.forName(charsetAsString));
			
			final ClosureRequestMapping closureRequestMapping;
			
			// Check if requestMapping is in thread local
			if (HotContext.getRequestMapping() == null) {
				closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(req);
			} else {
				closureRequestMapping = HotContext.getRequestMapping();
			}
			
			final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			
			if (closureRequestMapping != null) {
				final ExecutorService showEventLoop = closureRequestMapping.getEventLoop();
				
				// Async execution
				IOUtils.asyncRead(req, showEventLoop, showEventLoop)
				.done(new DoneCallback<byte[]>() {
					@SuppressWarnings({ "rawtypes" })
					@Override
					public void onDone(byte[] body) {
						try {
							Object response = handleRequest(closureRequestMapping, req, body, authentication);
							if (LOGGER.isDebugEnabled())
								LOGGER.debug("Response type: "+response.getClass());
							
							if (response instanceof NativeJavaObject) {
								response = ((NativeJavaObject) response).unwrap();
							}
							if (response instanceof Promise) {
								Promise promise = (Promise) response;
								promise._done(new DCallback() {
									@Override
									public void onDone(Object result) {
										handleResponse(result, finalMediaType , resp, async, showEventLoop);
									}
								})._fail(new FCallback() {
									@Override
									public void onFail(Object object) {
										LOGGER.debug("Exception type "+object.getClass());
										Exception exception = null;
										Object o = null;
										try {
											// If multiple values in fail callback, we take the first one
											if (object instanceof Object[]) {
												o =  ((Object[])object)[0];
											} else {
												o = object;
											}
											if (o instanceof NativeJavaObject) {
												o = ((NativeJavaObject) o).unwrap();
											}
											if (o instanceof Throwable) {
												exception = new Exception((Throwable) o);
											} else {
												handleResponse(o, finalMediaType, resp, async, showEventLoop);
												return;
											}
										} catch (Exception e) {
											exception = e;
										}
										if (LOGGER.isDebugEnabled()) 
											LOGGER.debug("",exception);
										resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
										writeBytesToResponseAsync(resp, extractStackTrace(exception).getBytes(),async, showEventLoop);
//										async.complete();
									}
								});
							} else {
								handleResponse(response, finalMediaType, resp, async, showEventLoop);
							}
						} catch (Exception e) {
							if (LOGGER.isDebugEnabled()) 
								LOGGER.debug("",e);
							resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
							writeBytesToResponseAsync(resp, extractStackTrace(e).getBytes(), async, showEventLoop);
						}
					}
				}).fail(new FailCallback<Exception>() {
					@Override
					public void onFail(Exception exception) {
						if (LOGGER.isDebugEnabled()) 
							LOGGER.debug("",exception);
						resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
						writeBytesToResponseAsync(resp, extractStackTrace(exception).getBytes(), async, showEventLoop);
					}
				});
			
			} else {
				resp.setStatus(HttpStatus.NOT_FOUND.value());
				writeBytesToResponseAsync(resp, "No closure matching the request".getBytes(), async, httpIOEventLoop);
			}
		} catch (Exception e) {
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			writeBytesToResponseAsync(resp, extractStackTrace(e).getBytes(), async, httpIOEventLoop);
		}
	}
	
	private void initSpringBeans () {
		if (closureRequestMappingHandlerMapping == null) {
			try {
				ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
				closureRequestMappingHandlerMapping = applicationContext.getBean(ClosureRequestMappingHandlerMapping.class);
				httpDataSerializer = applicationContext.getBean(HttpDataSerializer.class);
				groovyDataConverter = applicationContext.getBean(GroovyMapConverter.class);
				pyDictionaryConverter = applicationContext.getBean(PyDictionaryConverter.class);
				jsDataConverter = applicationContext.getBean(JsMapConverter.class);
				groovyHttpDataDeserializer = applicationContext.getBean(GroovyHttpDataDeserializer.class);
				pythonHttpDataDeserializer = applicationContext.getBean(PythonHttpDataDeserializer.class);
				jsHttpDataDeserializer = applicationContext.getBean(JsHttpDataDeserializer.class);
				blockingTreadPool = (ExecutorService) applicationContext.getBean("blockingTasksThreadPool");
				httpIOEventLoop = (ExecutorService) applicationContext.getBean("httpIOEventLoop");
			} catch (BeansException e) {
				LOGGER.error("Failed to init spring beans",e);
			}
		}
	}
	
	private Object handleRequest(ClosureRequestMapping closureRequestMapping, HttpServletRequest httpServletRequest, byte[] body, Authentication authentication) {
		
		RestRequest<?> restRequest;
		
		if (closureRequestMapping.getClosure() instanceof GroovyClosure) {
			 restRequest = new GroovyRestRequest(closureRequestMapping.getOptions(), groovyDataConverter, groovyHttpDataDeserializer, httpServletRequest, body, authentication);
		} else if (closureRequestMapping.getClosure() instanceof PythonClosure) {
			restRequest = new PythonRestRequest(closureRequestMapping.getOptions(), pyDictionaryConverter, pythonHttpDataDeserializer, httpServletRequest, body, authentication);
		} else if (closureRequestMapping.getClosure() instanceof JSClosure) {
			restRequest = new JSRestRequest(closureRequestMapping.getOptions(), jsDataConverter, jsHttpDataDeserializer, httpServletRequest, body, authentication);
		} else {
			throw new RuntimeException("showClosure is in the wrong type "+closureRequestMapping.getClosure().getClass());
		}
		if (LOGGER.isDebugEnabled()) LOGGER.debug("Executing closure");
		return closureRequestMapping.getClosure().call(restRequest);
	}
	
	private void handleResponse (Object objectResponse, 
			MediaType acceptContentType, 
			HttpServletResponse resp, 
			AsyncContext async, 
			ExecutorService showEventLoop) {
		
		Object convertedResponse = objectResponse;
		
		// JS script responses
		if (objectResponse instanceof NativeObject) {
			convertedResponse = jsDataConverter.toMap((NativeObject) objectResponse);
		} else if (objectResponse instanceof NativeArray) {
			convertedResponse = jsDataConverter.toListMap((NativeArray) objectResponse);
		} else if (objectResponse instanceof PyDictionary) {
			convertedResponse = pyDictionaryConverter.toMap((PyDictionary) objectResponse);
		}
		
		try {
			if (convertedResponse == null)
				writeBytesToResponseAsync(resp, "".getBytes(), async, showEventLoop);
			else if (convertedResponse instanceof Response) {
				Response response = (Response) convertedResponse;
				Map<?,?> headers = response.getHeaders();
				Object content = response.getBody();
				MediaType extractedResponseContentType = extractContentType(headers);
				
				resp.setStatus(response.getStatus());
				resp.setContentType(
						extractedResponseContentType == null?acceptContentType.toString():extractedResponseContentType.toString());
				
				for (Entry<?, ?> entry : headers.entrySet()) {
					resp.setHeader(entry.getKey().toString(), entry.getValue().toString());
				}
				byte[] body = httpDataSerializer.serialize(
						content, 
						extractedResponseContentType == null?acceptContentType:extractedResponseContentType);
				
				writeBytesToResponseAsync(resp, body, async, showEventLoop);
			} else {
				byte[] body = httpDataSerializer.serialize(convertedResponse, acceptContentType);
				resp.setContentType(acceptContentType.toString());
				writeBytesToResponseAsync(resp, body, async, showEventLoop);
			}
		} catch (Exception e) {
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			writeBytesToResponseAsync(resp, extractStackTrace(e).getBytes(), async, showEventLoop);
		}
	}
	
	private MediaType extractContentType (Map<?, ?> headers) {
		for (Entry<?, ?> entry : headers.entrySet()) {
			if (entry.getKey().toString().equals(com.google.common.net.HttpHeaders.CONTENT_TYPE)) {
				MediaType mediaType = MediaType.parseMediaType(entry.getValue().toString());
				if (mediaType.getCharSet() == null && !HttpDataSerializer.byteMediaTypes.contains(mediaType.toString())) {
					return mediaType = MediaType.parseMediaType(entry.getValue().toString()+"; charset="+DEFAULT_CHARSET);
				}
				return mediaType;
			}
		}
		return null;
	}
	
	protected String extractStackTrace (Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}
	
	private void writeBytesToResponseAsync(HttpServletResponse httpServletResponse, byte[] bytes, final AsyncContext async, final ExecutorService eventLoop) {
		
		try {
			final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			final ServletOutputStream outputStream;

			if (httpServletResponse instanceof SaveContextOnUpdateOrErrorResponseWrapper) {
				((SaveContextOnUpdateOrErrorResponseWrapper) httpServletResponse).getResponse();
				outputStream = ((SaveContextOnUpdateOrErrorResponseWrapper) httpServletResponse).getResponse().getOutputStream();
			} else {
				outputStream = httpServletResponse.getOutputStream();
			}

			outputStream.setWriteListener(new WriteListener() {

				@Override
				public void onWritePossible() throws IOException {
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							try {
								byte[] buffer = new byte[2048];
								int len = 0;
								while (outputStream.isReady() && (len = bais.read(buffer)) != -1) {
									outputStream.write(buffer, 0, len);
								}
								if (len == -1) {
									async.complete();
								}
							} catch (IOException e) {
								LOGGER.error("", e);
								async.complete();
							}
						}
					});
				}
				@Override
				public void onError(Throwable t) {
					LOGGER.error("", t);
					async.complete();
				}
			});
			
		} catch (IOException e) {
			LOGGER.error("", e);
			async.complete();
		}
	}
	
	private void writeBytesToResponse(HttpServletResponse httpServletResponse, byte[] bytes, final AsyncContext async) {
		try {
			httpServletResponse.getOutputStream().write(bytes);
		} catch (IOException e) {
			LOGGER.error("",e);
		} finally {
			async.complete();
		}
	}
}
