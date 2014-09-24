package be.icode.hot.test.shows;

import static org.junit.Assert.*

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.StatusCode
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.junit.Test
import org.springframework.web.servlet.DispatcherServlet


class TestWebsocket {

	def createServer = {
		Server server = new Server(8080)
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		server.setHandler(servletContextHandler)
		
		
		ServletHolder restHolder = new ServletHolder(DispatcherServlet.class)
		restHolder.name = "hot-rest"
		restHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
		restHolder.initParameters["contextConfigLocation"] = "be.icode.hot.spring.config.WebSocketConfig"
		servletContextHandler.addServlet(restHolder, "/socket/*")
		
		server
	}
	
	def createClient = { url ->
		WebSocketClient socketclient = new WebSocketClient()
		socketclient.start()
		def socket = new EchoSocket()
		socketclient.connect(socket, new URI(url))
		socket
	}
	
	@Test
	public void testWebSocketConfig() {
		def server = createServer()
		server.start()
		
		def socket = createClient ("ws://localhost:8080/socket/chatroom")
		
		sleep(10000)
		
		assert socket.response == "Hello"
		
		server.stop()
	}

	@WebSocket
	class EchoSocket {
		
		def response = "toto"
		
		@OnWebSocketConnect
		void onConnect(Session session) {
			try {
				Future<Void> fut;
				fut = session.getRemote().sendStringByFuture("Hello");
				fut.get(1, TimeUnit.SECONDS);
				session.close(StatusCode.NORMAL, "I'm done");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		@OnWebSocketMessage
		void onMessage(String msg) {
			response = msg
		}
	}
}
