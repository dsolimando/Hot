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

import java.util.concurrent.Executors

import javax.servlet.DispatcherType

import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy

import be.solidx.hot.DataConverter
import be.solidx.hot.nio.http.GroovyHttpClient
import be.solidx.hot.nio.http.HttpDataSerializer
import be.solidx.hot.nio.http.SSLContextBuilder
import be.solidx.hot.rest.ClientAuthServlet
import be.solidx.hot.shows.rest.RestClosureServlet
import be.solidx.hot.spring.config.SecurityConfig
import be.solidx.hot.spring.config.ShowConfig
import be.solidx.hot.spring.config.SocialConfig
import be.solidx.hot.spring.config.ThreadPoolsConfig
import be.solidx.hot.web.AsyncStaticResourceServlet

import com.thoughtworks.xstream.XStream


class TestSecuredRests {
	
	NioClientSocketChannelFactory cf =new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS)
	SSLContextBuilder sSLContextBuilder = new SSLContextBuilder()
	ObjectMapper objectMapper = new ObjectMapper()
	FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter()
	XStream xStream = new XStream()
	DataConverter dataConverter = new DataConverter()
	HttpDataSerializer httpDataSerializer = new HttpDataSerializer(formHttpMessageConverter, objectMapper, xStream, dataConverter)

	def createServer = {
		Server server = new Server(8080)
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		server.setHandler(servletContextHandler)
		
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext()
		rootContext.register(ShowConfig.class,SocialConfig.class, SecurityConfig.class)
		ContextLoaderListener listener = new ContextLoaderListener(rootContext)
		servletContextHandler.addEventListener(listener)
		
		
		FilterHolder filterHolder = new FilterHolder(DelegatingFilterProxy.class)
		filterHolder.name  = "springSecurityFilterChain"
		filterHolder.asyncSupported = true
		servletContextHandler.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.ASYNC, DispatcherType.REQUEST))
		
		ServletHolder restHolder = new ServletHolder(RestClosureServlet.class)
		restHolder.name = "hot-rest"
		servletContextHandler.addServlet(restHolder, "/rest/*")
		
		ServletHolder clientAuthHolder = new ServletHolder(ClientAuthServlet.class)
		clientAuthHolder.name = "client-auth"
		servletContextHandler.addServlet(clientAuthHolder, "/client-auth/*")
		
		ServletHolder staticHolder = new ServletHolder(AsyncStaticResourceServlet.class)
		staticHolder.name = "hot-static"
		servletContextHandler.addServlet(staticHolder, "/*")
		
		server
	}
	
	@Test
	public void testSecureBasicRestEndpoint() {
		System.setProperty("configFileLocation", "be/solidx/hot/test/shows/secured-config.json")
		
		def server = createServer()
		
		server.start()
		sleep 2000
		
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/secure-scells',
			type: 'GET',
			headers:[
				"Authorization":"Basic aG90OmhvdA=="
			]
		]
		
		def options2 = [
			url: 'http://localhost:8080/rest/scells',
			type: 'GET'
		]
		
		client.buildRequest (options).done { data, textStatus, response ->
			println response.headers
			assert textStatus == "OK"
		}.fail { response, status, error ->
			println status
		}.then { data, textStatus, response ->
			client.buildRequest (options2)
		}.done { data, textStatus, response ->
			println textStatus
			assert textStatus == "OK"
			options.headers = [:]
		}.then { data, textStatus, response->
			client.buildRequest (options)
		}.done { data, textStatus, response->
			println textStatus
			assert textStatus == "Full authentication is required to access this resource"
			println "Stopping server"
			server.stop()
		}
		server.threadPool.join()
	}

	@Test
	public void testSecureFormRestEndpoint() {
		System.setProperty("configFileLocation", "be/solidx/hot/test/shows/secured-config.json")
		
		def server = createServer()
		
		server.start()
		sleep 2000
		
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		
		def options = [
			url: 'http://localhost:8080/rest/secure-scells',
			headers: [
				'Accept':"text/html,application/xhtml"
			]
		]
		
		def options2 = [
			url: 'http://localhost:8080/rest-login',
			type: 'POST',
			data:[
				username:"hot",
				password:"hot"
			],
			headers: [
				'Content-type':"application/x-www-form-urlencoded",
				'Accept':"text/html,application/xhtml"
			]
		]
		
		def options3 = [
			url: "http://localhost:8080/logout",
			type: 'POST'
		]
		
		def cookie = null
		
		client.buildRequest (options).done { data, textStatus, response ->
			// Redirection to login page
			assert response.statusCode == 302
			assert response.headers['Location'] == "http://localhost/login.html"
		}.then { data, textStatus, response ->
			client.buildRequest (options2)
		}.done { data, textStatus, response ->
			assert response.headers['Location'] == "http://localhost/"
		}.then { data, textStatus, response ->
			cookie = response.headers["Set-Cookie"]
			options.headers = ["Cookie":cookie]
			client.buildRequest (options)
		}.then { data, textStatus, response ->
			// Logout
			options3.headers = ["Cookie":cookie]
			client.buildRequest (options3)
		}.then { data, textStatus, response ->
			client.buildRequest (options)
		}.done { data, textStatus, response ->
			assert textStatus == "Full authentication is required to access this resource"
			println "Stopping server"
			server.stop()
		}
		server.threadPool.join()
	}
	
	static def mongoTest = {
		def test = new TestSecuredRests()
		
		System.setProperty("configFileLocation", "be/solidx/hot/test/shows/secured-config-mongo.json")
		
		def server = test.createServer()
		
		server.start()
	}
	
	
	static def main(args) {
		
		def test = new TestSecuredRests()
		
		System.setProperty("configFileLocation", "be/solidx/hot/test/shows/secured-config.json")
		
		def server = test.createServer()
		
		server.start()
		
//		mongoTest()
	}
}
