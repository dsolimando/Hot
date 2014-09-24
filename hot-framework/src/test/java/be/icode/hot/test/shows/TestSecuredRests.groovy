package be.icode.hot.test.shows;

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

import be.icode.hot.DataConverter
import be.icode.hot.nio.http.GroovyHttpClient
import be.icode.hot.nio.http.HttpDataSerializer
import be.icode.hot.nio.http.SSLContextBuilder
import be.icode.hot.rest.ClientAuthServlet
import be.icode.hot.shows.rest.RestClosureServlet
import be.icode.hot.spring.config.SecurityConfig
import be.icode.hot.spring.config.ShowConfig
import be.icode.hot.spring.config.SocialConfig
import be.icode.hot.spring.config.ThreadPoolsConfig
import be.icode.hot.web.AsyncStaticResourceServlet

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
//		restHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
//		restHolder.initParameters["contextConfigLocation"] = "be.icode.hot.spring.config.RestShowConfig"
		servletContextHandler.addServlet(restHolder, "/rest/*")
		
		ServletHolder clientAuthHolder = new ServletHolder(ClientAuthServlet.class)
		clientAuthHolder.name = "client-auth"
		servletContextHandler.addServlet(clientAuthHolder, "/client-auth/*")
		
		ServletHolder staticHolder = new ServletHolder(AsyncStaticResourceServlet.class)
		staticHolder.name = "hot-static"
//		staticHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
//		staticHolder.initParameters["contextConfigLocation"] = "be.icode.hot.spring.config.ControllersConfig"
		servletContextHandler.addServlet(staticHolder, "/*")
		
		server
	}
	
	@Test
	public void testSecureBasicRestEndpoint() {
		System.setProperty("configFileLocation", "be/icode/hot/test/shows/secured-config.json")
		
		def server = createServer()
		
		server.start()
		sleep 2000
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
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
		System.setProperty("configFileLocation", "be/icode/hot/test/shows/secured-config.json")
		
		def server = createServer()
		
		server.start()
		sleep 2000
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		
		def options = [
			url: 'http://localhost:8080/rest/secure-scells'
		]
		
		def options2 = [
			url: 'http://localhost:8080/login',
			type: 'POST',
			data:[
				username:"hot",
				password:"hot"
			],
			headers: [
				'Content-type':"application/x-www-form-urlencoded"
			]
		]
		
		def options3 = [
			url: "http://localhost:8080/logout",
			type: 'POST'
		]
		
		client.buildRequest (options).done { data, textStatus, response ->
			println response.headers
			assert textStatus == "Full authentication is required to access this resource"
		}.done { data, textStatus, response ->
			println response.headers
		}.then { data, textStatus, response ->
			client.buildRequest (options2)
		}.done { data, textStatus, response ->
			println response.headers
		}.then { data, textStatus, response ->
			options.headers = ["Cookie":response.headers["Set-Cookie"]]
			client.buildRequest (options)
		}.then { data, textStatus, response ->
			client.buildRequest (options3)
		}.then { data, textStatus, response ->
			options.headers = [:]
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
		
		System.setProperty("configFileLocation", "be/icode/hot/test/shows/secured-config-mongo.json")
		
		def server = test.createServer()
		
		server.start()
	}
	
	
	static def main(args) {
		
//		def test = new TestSecuredRests()
//		
//		System.setProperty("configFileLocation", "be/icode/hot/test/shows/secured-config.json")
//		
//		def server = test.createServer()
//		
//		server.start()
		
		mongoTest()
	}
}
