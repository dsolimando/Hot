package be.solidx.hot.test.shows;

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
		Server server = new Server(18080)
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		server.setHandler(servletContextHandler)
		
		
		ServletHolder restHolder = new ServletHolder(DispatcherServlet.class)
		restHolder.name = "hot-rest"
		restHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
		restHolder.initParameters["contextConfigLocation"] = "be.solidx.hot.spring.config.ShowConfig,be.solidx.hot.spring.config.WebSocketConfig"
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
		
		def socket = createClient ("ws://localhost:18080/socket/chatroom")
		
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
