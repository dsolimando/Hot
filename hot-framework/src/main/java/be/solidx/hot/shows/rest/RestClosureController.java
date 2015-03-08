package be.solidx.hot.shows.rest;
//package be.icode.hot.shows.rest;
//
//import hot.Response;
//
//import java.nio.charset.Charset;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.Callable;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.jdeferred.DeferredManager;
//import org.jdeferred.DoneCallback;
//import org.jdeferred.FailCallback;
//import org.jdeferred.impl.DefaultDeferredManager;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.context.request.async.DeferredResult;
//
//import be.icode.hot.groovy.GroovyClosure;
//import be.icode.hot.groovy.GroovyMapConverter;
//import be.icode.hot.js.JSClosure;
//import be.icode.hot.js.JsMapConverter;
//import be.icode.hot.nio.http.HttpDataSerializer;
//import be.icode.hot.promises.Promise;
//import be.icode.hot.promises.Promise.DCallback;
//import be.icode.hot.promises.Promise.FCallback;
//import be.icode.hot.python.PyDictionaryConverter;
//import be.icode.hot.python.PythonClosure;
//import be.icode.hot.rest.RestController;
//import be.icode.hot.shows.ClosureRequestMapping;
//import be.icode.hot.shows.RestRequest;
//import be.icode.hot.shows.groovy.GroovyRestRequest;
//import be.icode.hot.shows.javascript.JSRestRequest;
//import be.icode.hot.shows.python.PythonRestRequest;
//import be.icode.hot.shows.spring.ClosureRequestMappingHandlerMapping;
//import be.icode.hot.utils.GroovyHttpDataDeserializer;
//import be.icode.hot.utils.JsHttpDataDeserializer;
//import be.icode.hot.utils.PythonHttpDataDeserializer;
//
//@Controller
//@Deprecated
//public class RestClosureController extends RestController {
//	
//	ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;
//	
//	HttpDataSerializer httpDataSerializer;
//	
//	GroovyMapConverter groovyDataConverter;
//	
//	PyDictionaryConverter pyDictionaryConverter;
//	
//	JsMapConverter jsDataConverter;
//	
//	GroovyHttpDataDeserializer groovyHttpDataDeserializer;
//	
//	PythonHttpDataDeserializer pythonHttpDataDeserializer;
//	
//	JsHttpDataDeserializer jsHttpDataDeserializer;
//
//	private String acceptMediaTypeAsString;
//
//	public RestClosureController(
//			ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping, 
//			HttpDataSerializer httpDataSerializer, 
//			GroovyMapConverter groovyDataConverter,
//			PyDictionaryConverter dictionaryConverter, 
//			JsMapConverter jsDataConverter,
//			GroovyHttpDataDeserializer groovyHttpDataDeserializer,
//			PythonHttpDataDeserializer pythonHttpDataDeserializer,
//			JsHttpDataDeserializer jsHttpDataDeserializer) {
//		
//		this.closureRequestMappingHandlerMapping = closureRequestMappingHandlerMapping;
//		this.httpDataSerializer = httpDataSerializer;
//		this.groovyDataConverter = groovyDataConverter;
//		this.pyDictionaryConverter = dictionaryConverter;
//		this.jsDataConverter = jsDataConverter;
//		this.groovyHttpDataDeserializer = groovyHttpDataDeserializer;
//		this.pythonHttpDataDeserializer = pythonHttpDataDeserializer;
//		this.jsHttpDataDeserializer = jsHttpDataDeserializer;
//	}
//
//	@RequestMapping(value = "/**")
//	synchronized public DeferredResult<ResponseEntity<byte[]>> handleRestRequest (final WebRequest webRequest, final HttpServletRequest request) throws Exception {
//		
//		final DeferredResult<ResponseEntity<byte[]>> deferredResult = new DeferredResult<ResponseEntity<byte[]>>();
//		
//		Charset charset = null;
////		String acceptEncoding = null;
////		try {
////			acceptEncoding = webRequest.getHeader(com.google.common.net.HttpHeaders.ACCEPT_ENCODING);
////			if (acceptEncoding == null) acceptEncoding = DEFAULT_CHARSET;
////			else charset = Charset.forName(acceptEncoding);
////		} catch (Exception e1) {
////			deferredResult.setResult(new ResponseEntity<byte[]>(String.format("Character encoding not supported: %s", acceptEncoding).getBytes(),HttpStatus.NOT_ACCEPTABLE));
////			return deferredResult;
////		}
//		
//		acceptMediaTypeAsString = webRequest.getHeader(com.google.common.net.HttpHeaders.ACCEPT);
//		if (acceptMediaTypeAsString == null) acceptMediaTypeAsString = DEFAULT_ACCEPT+";"+ DEFAULT_CHARSET;
//		
//		final List<MediaType> acceptMediaTypes = MediaType.parseMediaTypes(acceptMediaTypeAsString);
//		final Charset finalCharset = charset;
//
//		final ClosureRequestMapping closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(request);
//		
//		if (closureRequestMapping != null) {
//
//			// Async execution
//			DeferredManager deferredManager = new DefaultDeferredManager(closureRequestMapping.getEventLoop());
//			deferredManager.when(new Callable<Object>() {
//				@Override
//				public Object call() throws Exception {
//					return handleRequest(closureRequestMapping, webRequest, request);
//				}
//			}).done(new DoneCallback<Object>() {
//				@SuppressWarnings({ "rawtypes" })
//				@Override
//				public void onDone(Object response) {
//					if (response instanceof Promise) {
//						Promise promise = (Promise) response;
//						promise._done(new DCallback() {
//							@Override
//							public void onDone(Object result) {
//								System.out.println("before http finish" +Thread.currentThread());
//								try {
//									deferredResult.setResult(handleResponse(result, finalCharset, acceptMediaTypes.get(0)));
//								} catch (Exception e) {
//									deferredResult.setErrorResult(buildErrorResponse(e));
//								}
//							}
//						})._fail(new FCallback() {
//							@Override
//							public void onFail(Throwable throwable) {
//								deferredResult.setErrorResult(buildErrorResponse(new Exception(throwable)));
//							}
//						});
//					} else {
//						try {
//							deferredResult.setResult(handleResponse(response, finalCharset, acceptMediaTypes.get(0)));
//						} catch (Exception e) {
//							deferredResult.setErrorResult(buildErrorResponse(e));
//						}
//					}
//				}
//			}).fail(new FailCallback<Throwable>() {
//				@Override
//				public void onFail(Throwable throwable) {
//					deferredResult.setErrorResult(buildErrorResponse(new Exception(throwable)));
//				}
//			});
//		
//		} else {
//			deferredResult.setErrorResult(buildErrorResponse(new Exception("No Closure found...")));
//		}
//		return deferredResult;
//	}
//	
//	private Object handleRequest(ClosureRequestMapping closureRequestMapping, WebRequest webRequest, HttpServletRequest httpServletRequest) {
//		
//		RestRequest<?> restRequest;
//		
//		if (closureRequestMapping.getClosure() instanceof GroovyClosure) {
//			 restRequest = new GroovyRestRequest(closureRequestMapping.getOptions(), groovyDataConverter, groovyHttpDataDeserializer, httpServletRequest);
//		} else if (closureRequestMapping.getClosure() instanceof PythonClosure) {
//			restRequest = new PythonRestRequest(closureRequestMapping.getOptions(), pyDictionaryConverter, pythonHttpDataDeserializer, httpServletRequest);
//		} else if (closureRequestMapping.getClosure() instanceof JSClosure) {
//			restRequest = new JSRestRequest(closureRequestMapping.getOptions(), jsDataConverter, jsHttpDataDeserializer, httpServletRequest);
//		} else {
//			throw new RuntimeException("showClosure is in the wrong type "+closureRequestMapping.getClosure().getClass());
//		}
//		return closureRequestMapping.getClosure().call(restRequest);
//	}
//	
//	@SuppressWarnings({ "rawtypes" })
//	private ResponseEntity<byte[]> handleResponse (Object objectResponse, Charset requestedCharset, MediaType acceptContentType) throws Exception {
//		
//		if (objectResponse == null)
//			return buildEmptyResponse(HttpStatus.OK);
//		else if (objectResponse instanceof Response) {
//			Response response = (Response) objectResponse;
//			Map headers = response.getHeaders();
//			Object content = response.getBody();
//			MediaType extractedResponseContentType = extractContentType(headers);
//			Integer status = response.getStatus();
//			
//			byte[] body = httpDataSerializer.serialize(content, extractedResponseContentType == null?acceptContentType:extractedResponseContentType);
//			return buildResponse(body, headers, status);
//		} else {
//			byte[] body = httpDataSerializer.serialize(objectResponse, acceptContentType);
//			HttpHeaders httpheaders = new HttpHeaders();
//			httpheaders.put(com.google.common.net.HttpHeaders.CONTENT_TYPE, Arrays.asList(acceptContentType.toString()));
//			return new ResponseEntity<byte[]>(body, httpheaders, HttpStatus.OK);
//		}
//	}
//	
//	private MediaType extractContentType (Map<?, ?> headers) {
//		for (Entry<?, ?> entry : headers.entrySet()) {
//			if (entry.getKey().toString().equals(com.google.common.net.HttpHeaders.CONTENT_TYPE)) {
//				MediaType mediaType = MediaType.parseMediaType(entry.getValue().toString());
//				if (mediaType.getCharSet() == null && !HttpDataSerializer.byteMediaTypes.contains(mediaType.toString())) {
//					return mediaType = MediaType.parseMediaType(entry.getValue().toString()+"; charset="+DEFAULT_CHARSET);
//				}
//				return mediaType;
//			}
//		}
//		return null;
//	}
//	
//	
//	public static void main(String[] args) {
//		MediaType mt = MediaType.parseMediaType("image/png");
//		System.out.println(mt);
//	}
//}
