package be.solidx.hot.test.shows;

import static org.junit.Assert.*

import java.util.concurrent.Executors

import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext

import be.solidx.hot.DataConverter
import be.solidx.hot.nio.http.GroovyHttpClient
import be.solidx.hot.nio.http.HttpDataSerializer
import be.solidx.hot.nio.http.SSLContextBuilder
import be.solidx.hot.shows.rest.RestClosureServlet
import be.solidx.hot.spring.config.ShowConfig
import be.solidx.hot.spring.config.ThreadPoolsConfig

import com.thoughtworks.xstream.XStream


class TestRESTServices {

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
		rootContext.register(ShowConfig.class)
		ContextLoaderListener listener = new ContextLoaderListener(rootContext)
		servletContextHandler.addEventListener(listener)
		
		ServletHolder restHolder = new ServletHolder(RestClosureServlet.class)
		restHolder.name = "hot-rest"
//		restHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
//		restHolder.initParameters["contextConfigLocation"] = "be.solidx.hot.spring.config.RestShowConfig"
		servletContextHandler.addServlet(restHolder, "/rest/*")
		
		server
	}
	
	@Test
	public void test1() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/jsitem',
			type: 'POST',
			headers:[
				'Accept':"application/json",
				'Content-Type':"application/x-www-form-urlencoded"
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == [items:[name:"lilou", age:8]]
			server.stop()
		}).fail({ response, status, error ->
			println status
		})
		server.threadPool.join()
		server.stop()
	}
	
	@Test
	public void testRequestParam() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells/3',
			type: 'GET',
			headers:[
				'Accept':"application/json"
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == [scells:[
				title:"baskets", 
				description:'dslfjksdlfjslfjslfdjslfd',
				id: "3"
				]
			]
			server.stop()
		}).fail({ response, status, error ->
			println status
			server.stop()
		})
		server.threadPool.join()
		server.stop()
	}

	@Test
	public void testScellsJson() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells',
			type: 'GET',
			headers:[
				'Accept':"application/json"
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == [scells:[
				title:"baskets", 
				description:'dslfjksdlfjslfjslfdjslfd'
				]
			]
			server.stop()
		}).fail({ response, status, error ->
			println status
		})
		server.threadPool.join()
		server.stop()
	}
	
	@Test
	public void testScellsXml() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells',
			type: 'GET',
			headers:[
				'Accept':"application/xml"
			],
//			processResponse:false
		]
		def resp
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data.title == "baskets"
			assert data.description == 'dslfjksdlfjslfjslfdjslfd'
			server.stop()
		}).fail({ response, status, error ->
			println status
		})
		server.threadPool.join()
		server.stop()
	}
	
	@Test
	public void testScellsJsonDeferred() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells-slow',
			type: 'GET',
			headers:[
				'Accept':"application/json"
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == [scells:[
				title:"baskets",
				description:'dslfjksdlfjslfjslfdjslfd'
				]
			]
			server.stop()
		}).fail({ response, status, error ->
			println status
		})
		server.threadPool.join()
		server.stop()
	}
	
	@Test
	public void testScellsHtml() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells-index.html',
			type: 'GET',
			headers:[
				'Accept':"text/html"
			],
//			processResponse:false
		]
		def resp
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			server.stop()
		}).fail({ response, status, error ->
			error.printStackTrace()
		})
		server.threadPool.join()
		server.stop()
	}
	
	@Test
	public void testScellsJsonResponse() {
		def server = createServer()
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/rest/scells-response',
			type: 'GET',
			headers:[
				'Accept':"application/json"
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == [scells:[
				title:"baskets",
				description:'dslfjksdlfjslfjslfdjslfd'
				]
			]
			server.stop()
		}).fail({ response, status, error ->
			error.printStackTrace()
		})
		server.threadPool.join()
		server.stop()
	}
}
