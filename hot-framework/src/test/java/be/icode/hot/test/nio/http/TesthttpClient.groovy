package be.icode.hot.test.nio.http;

import static org.junit.Assert.*

import java.util.concurrent.Executors

import javax.script.CompiledScript
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.io.IOUtils
import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.junit.Test
import org.mozilla.javascript.Scriptable
import org.python.apache.xerces.jaxp.DocumentBuilderFactoryImpl
import org.springframework.http.converter.FormHttpMessageConverter

import be.icode.hot.DataConverter
import be.icode.hot.Script
import be.icode.hot.js.JSScriptExecutor
import be.icode.hot.js.JsMapConverter
import be.icode.hot.nio.http.GroovyHttpClient
import be.icode.hot.nio.http.HttpDataSerializer
import be.icode.hot.nio.http.JsHttpClient
import be.icode.hot.nio.http.PythonHttpClient
import be.icode.hot.nio.http.SSLContextBuilder
import be.icode.hot.python.PyDictionaryConverter
import be.icode.hot.python.PythonScriptExecutor
import be.icode.hot.spring.config.ThreadPoolsConfig

import com.thoughtworks.xstream.XStream

class TesthttpClient {

	NioClientSocketChannelFactory cf =new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS)
	SSLContextBuilder sSLContextBuilder = new SSLContextBuilder()
	ObjectMapper objectMapper = new ObjectMapper()
	FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter()
	XStream xStream = new XStream()
	DataConverter dataConverter = new DataConverter()
	HttpDataSerializer httpDataSerializer = new HttpDataSerializer(formHttpMessageConverter, objectMapper, xStream, dataConverter)
	
	def server = { servletClass ->
		Server server = new Server(8080)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(servletClass)
		holder.name = "postServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server
	}
	
	@Test
	void testGroovyHttpClientGetServlet() {
		
		def server = server(GetServlet.class)
		server.start()
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'GET',
			headers:[
				'Content-type':"application/x-www-form-urlencoded"
			],
			data:[
				name:'Damien',
				age:8
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert new String(data) == 'Damien 8'
			println "Stopping server"
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostJsonServlet() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/json"
			],
			data:[
				name:'Damien',
				age:8
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data.name == 'Damien' && data.age == 8
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostJsonServletNoProcess() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/json"
			],
			data:'{"name":"Damien","age":8}',
			processData: false
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data.name == 'Damien' && data.age == 8
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostXMLServlet() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/xml"
			],
			data:[
				name:'Damien',
				age:8
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data.name == 'Damien' && data.age == 8
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostXMLServletNoProcess() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf , sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/xml"
			],
			data:'<root><name>Damien</name><age>8</age></root>',
			processData:false
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data.name == 'Damien' && data.age == 8
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostFormServlet() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/x-www-form-urlencoded"
			],
			data:[
				name:'Damien',
				age:8
			]
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			assert data == 'name=Damien&age=8'
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	void testGroovyHttpClientPostFormServletNoProcess() {
		Server server = server(EchoPOSTServlet.class)
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'http://localhost:8080/test',
			type: 'POST',
			headers:[
				'Content-type':"application/x-www-form-urlencoded"
			],
			data:'name=Damien&age=8',
			processData: false
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			assert data == 'name=Damien&age=8'
			server.stop();
		}).fail({ response, status, error ->
			println status
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	public void testGroovyHttpsClientPkcs12ClientXmlServlet() {
		def server = createHttpsServer(true)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(XmlServlet.class)
		holder.name = "xmlServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'https://localhost:8443/test',
			ca:'ca/ca.crt',
			p12:"ca/client.p12",
			passphrase:"client"
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data.@attr
			server.stop();
		}).fail({ response, status, error ->
			error.printStackTrace()
			println error.message
			server.stop();
		}) 
		server.threadPool.join()
	}
	
	def createHttpsServer = { needClientAuth ->
		Server server = new Server()
		
		// ssl context
		def sslcontextFactory = new SslContextFactory()
		sslcontextFactory.keyStorePath = getClass().getResource("/ca/jetty.jks").getPath()
		sslcontextFactory.keyManagerPassword = "jetty"
		sslcontextFactory.keyStorePassword = "jettyjetty"
		sslcontextFactory.needClientAuth = needClientAuth
		
		def https = new ServerConnector(server,
			new SslConnectionFactory(sslcontextFactory, "http/1.1"),
			new HttpConnectionFactory())
		https.setPort(8443);
		
		server.addConnector(https)
		
		server
	}
	
	@Test
	public void testGroovyHttpsClientPkcs12ClientJSONServlet() {
		def server = createHttpsServer(true)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(JsonServlet.class)
		holder.name = "xmlServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'https://localhost:8443/test',
			ca:'ca/ca.crt',
			p12:"ca/client.p12",
			passphrase:"client"
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data.name
			server.stop();
		}).fail({ response, status, error ->
			println error.message
			server.stop();
		})
		server.threadPool.join()
	}
	
	@Test
	public void testGroovyHttpsClientPkcs12ClientJSONServletNoProcess() {
		def server = createHttpsServer(true)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(JsonServlet.class)
		holder.name = "xmlServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server.start()
		
		GroovyHttpClient client = new GroovyHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer)
		def options = [
			url: 'https://localhost:8443/test',
			ca:'ca/ca.crt',
			p12:"ca/client.p12",
			passphrase:"client",
			processResponse:false
		]
		client.buildRequest (options).done({ data, textStatus, response ->
			print data
			server.stop();
		}).fail({ response, status, error ->
			println error.message
			server.stop();
		})
		server.threadPool.join()
	}
	
	def runJsHttpsClient = { scriptname, server ->
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor()
		DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		def params = [
			client:new JsHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer , dbuilder, jsScriptExecutor.globalScope, new JsMapConverter()),
			server: server]
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/icode/hot/test/nio/http/js/${scriptname}"));
		Script<Scriptable> script = new Script<Scriptable>(scriptString.getBytes(), scriptname);
		jsScriptExecutor.execute(script,params);
	}
	
	@Test
	public void testJSHttpClientGET() {
		Server server = server(GetServlet.class)
		server.start()
		
		runJsHttpsClient "http-client-get.js", server
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpClientPOSTForm() {
		Server server = server(GetServlet.class)
		server.start()
		
		runJsHttpsClient("http-client-post-form.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpClientPOSTJson() {
		Server server = server(EchoPOSTServlet.class)
		server.start()
		
		runJsHttpsClient("http-client-post-json.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpClientPOSTXML() {
		Server server = server(EchoPOSTServlet.class)
		server.start()
		
		runJsHttpsClient("http-client-post-xml.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientSelfSigned() {
		def server = createHttpsServer(false)
		
		server.start()
		
		runJsHttpsClient("https-client-selfsigned.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientJKS() {
		def server = createHttpsServer(false)
		server.start()
		
		runJsHttpsClient("https-client-jks.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientJKSClientAuth() {
		def server = createHttpsServer(false)
		server.start()
		
		runJsHttpsClient("https-client-server-jks.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientKeycertClientAuth() {
		def server = createHttpsServer(true)
		server.start()
		
		runJsHttpsClient("https-client-server-jks.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientPkcs12ClientAuth() {
		def server = createHttpsServer(true)
		server.start()
		
		runJsHttpsClient("https-client-server-pkcs12.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientPkcs12ClientAuthFail() {
		def server = createHttpsServer(true)
		server.start()
		
		runJsHttpsClient("https-client-jks.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientPkcs12ClientXmlServlet() {
		def server = createHttpsServer(true)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(XmlServlet.class)
		holder.name = "xmlServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server.start()
		
		runJsHttpsClient("https-client-server-pkcs12-xml-servlet.js", server)
		
		server.threadPool.join()
	}
	
	@Test
	public void testJSHttpsClientPkcs12ClientJSONServlet() {
		def server = createHttpsServer(true)
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		ServletHolder holder = new ServletHolder(JsonServlet.class)
		holder.name = "xmlServlet"
		servletContextHandler.addServlet(holder, "/test")
		
		server.start()
		
		runJsHttpsClient("https-client-server-pkcs12-json-servlet.js", server)
		
		server.threadPool.join()
	}
	
	def runPyHttpsClient = { scriptname, server ->
		PythonScriptExecutor pyScriptExecutor = new PythonScriptExecutor()
		DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		DocumentBuilderFactory f = new DocumentBuilderFactoryImpl()
		f.newDocumentBuilder()
		def params = [
			client:new PythonHttpClient(cf, sSLContextBuilder, objectMapper, httpDataSerializer , f.newDocumentBuilder(), new PyDictionaryConverter()),
			server: server
		]
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/icode/hot/test/nio/http/py/${scriptname}"));
		Script<CompiledScript> script = new Script<CompiledScript>(scriptString.getBytes(), scriptname);
		pyScriptExecutor.execute(script,params);
	}
	
	@Test
	public void testPyHttpClientGET() {
		Server server = server(GetServlet.class)
		server.start()
		
		runPyHttpsClient 'http-client-get.py', server
		
		server.threadPool.join()
	}
	
	@Test
	public void testPyHttpClientPOSTForm() {
		Server server = server(EchoPOSTServlet.class)
		server.start()
		
		runPyHttpsClient 'http-client-post-form.py', server
		
		server.threadPool.join()
	}
	
	@Test
	public void testPyHttpClientPOSTJson() {
		Server server = server(EchoPOSTServlet.class)
		server.start()
		
		runPyHttpsClient 'http-client-post-json.py', server
		
		server.threadPool.join()
	}
	
	@Test
	public void testPyHttpClientPOSTXml() {
		Server server = server(EchoPOSTServlet.class)
		server.start()
		
		runPyHttpsClient 'http-client-post-xml.py', server
		
		server.threadPool.join()
	}
}
