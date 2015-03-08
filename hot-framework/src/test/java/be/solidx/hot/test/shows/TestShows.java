package be.solidx.hot.test.shows;

import groovy.lang.Closure;

import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mozilla.javascript.NativeFunction;
import org.python.core.PyFunction;
import org.springframework.web.socket.WebSocketSession;

import be.solidx.hot.groovy.GroovyScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.python.PythonScriptExecutor;
import be.solidx.hot.shows.AbstractWebSocketHandler;
import be.solidx.hot.shows.ClosureRequestMapping;
import be.solidx.hot.shows.WebSocket.Handler;
import be.solidx.hot.shows.groovy.GroovyShow;
import be.solidx.hot.shows.javascript.JSShow;
import be.solidx.hot.shows.python.PythonShow;

public class TestShows {
	
	GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor();
	
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
	
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();

	@Test
	public void testGroovyShow() throws Exception {
		GroovyShow groovyShow = new GroovyShow(getClass().getResource("/requestMappingTests/groovyshow.show.groovy"),Executors.newFixedThreadPool(1),Executors.newCachedThreadPool(), null, groovyScriptExecutor,null,null);
		Assert.assertEquals(3, groovyShow.getRest().getRequestMappings().size());
		
		ClosureRequestMapping requestMapping = groovyShow.getRest().getRequestMappings().get(0);
		Assert.assertEquals("/items", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/xml", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
		
		requestMapping = groovyShow.getRest().getRequestMappings().get(1);
		Assert.assertEquals("/items", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/json", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
	
		requestMapping = groovyShow.getRest().getRequestMappings().get(2);
		Assert.assertEquals(0, requestMapping.getHeaders().size());
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertTrue(requestMapping.isAuth());
		
		Assert.assertEquals(groovyShow.getWebsocket().getSocketHandlerAdapterMap().size(), 1);
		for (Handler<Closure<?>> handler : groovyShow.getWebsocket().getSocketHandlerAdapterMap().values()) {
			AbstractWebSocketHandler<?> abstractHandler = (AbstractWebSocketHandler<?>) handler;
			Assert.assertNotNull(abstractHandler.getSocketHandlerAdapter());
			abstractHandler.getSocketHandlerAdapter().afterConnectionEstablished(Mockito.mock(WebSocketSession.class));
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testPythonShow() throws Exception {
		PythonShow pythonShow = new PythonShow(getClass().getResource("/requestMappingTests/pythonshow.show.py"),Executors.newFixedThreadPool(1),Executors.newCachedThreadPool(),null, pythonScriptExecutor,null,null);
		Assert.assertEquals(3, pythonShow.getRest().getRequestMappings().size());
		
		ClosureRequestMapping requestMapping = pythonShow.getRest().getRequestMappings().get(0);
		Assert.assertEquals("/pyitems", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/xml", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
		
		requestMapping = pythonShow.getRest().getRequestMappings().get(1);
		Assert.assertEquals("/pyitems", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/json", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
	
		requestMapping = pythonShow.getRest().getRequestMappings().get(2);
		Assert.assertEquals(0, requestMapping.getHeaders().size());
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertTrue(requestMapping.isAuth());
		
		Assert.assertEquals(pythonShow.getWebsocket().getSocketHandlerAdapterMap().size(), 1);
		for (Handler<PyFunction> handler : pythonShow.getWebsocket().getSocketHandlerAdapterMap().values()) {
			AbstractWebSocketHandler<?> abstractHandler = (AbstractWebSocketHandler) handler;
			Assert.assertNotNull(abstractHandler.getSocketHandlerAdapter());
			abstractHandler.getSocketHandlerAdapter().afterConnectionEstablished(Mockito.mock(WebSocketSession.class));
		}
	}
	
	@Test
	public void testJSShow() throws Exception {
		JSShow jsShow = new JSShow(getClass().getResource("/requestMappingTests/jsshow.show.js"),Executors.newFixedThreadPool(1), Executors.newCachedThreadPool(),null, jsScriptExecutor, null, null);
		Assert.assertEquals(4, jsShow.getRest().getRequestMappings().size());
		
		ClosureRequestMapping requestMapping = jsShow.getRest().getRequestMappings().get(0);
		Assert.assertEquals("/items", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/pdf", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
		
		requestMapping = jsShow.getRest().getRequestMappings().get(1);
		Assert.assertEquals("/items", requestMapping.getPaths().get(0));
		Assert.assertEquals(1, requestMapping.getHeaders().size());
		Assert.assertEquals("Content-Type: application/x-www-form-urlencoded", requestMapping.getHeaders().get(0));
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertFalse(requestMapping.isAuth());
		requestMapping.getClosure().call();
	
		requestMapping = jsShow.getRest().getRequestMappings().get(2);
		Assert.assertEquals(0, requestMapping.getHeaders().size());
		Assert.assertNotNull(requestMapping.getClosure());
		Assert.assertTrue(requestMapping.isAuth());
		
		Assert.assertEquals(jsShow.getWebsocket().getSocketHandlerAdapterMap().size(), 1);
		for (Handler<NativeFunction> handler : jsShow.getWebsocket().getSocketHandlerAdapterMap().values()) {
			AbstractWebSocketHandler<?> abstractHandler = (AbstractWebSocketHandler<?>) handler;
			Assert.assertNotNull(abstractHandler.getSocketHandlerAdapter());
			abstractHandler.getSocketHandlerAdapter().afterConnectionEstablished(Mockito.mock(WebSocketSession.class));
		}
	}
}
