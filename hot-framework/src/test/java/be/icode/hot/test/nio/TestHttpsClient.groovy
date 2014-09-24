//package be.icode.hot.test.nio
//
//import static org.junit.Assert.*
//
//import java.util.concurrent.Executors
//
//import javax.script.CompiledScript
//
//import org.apache.commons.io.IOUtils
//import org.eclipse.jetty.server.HttpConnectionFactory
//import org.eclipse.jetty.server.Server
//import org.eclipse.jetty.server.ServerConnector
//import org.eclipse.jetty.server.SslConnectionFactory
//import org.eclipse.jetty.util.ssl.SslContextFactory
//import org.junit.Test
//import org.springframework.core.convert.support.DefaultConversionService
//
//import be.icode.hot.Script
//import be.icode.hot.js.JSScriptExecutor
//import be.icode.hot.nio.groovy.GroovyHttpsClient
//import be.icode.hot.nio.javascript.JshttpsClient
//import be.icode.hot.nio.python.PythonHttpClient
//import be.icode.hot.nio.python.PythonHttpsClient
//import be.icode.hot.python.PythonScriptExecutor
//
//
//class TestHttpsClient {
//
//	def createHttpsServer = { needClientAuth ->
//		Server server = new Server()
//		
//		// ssl context
//		def sslcontextFactory = new SslContextFactory()
//		sslcontextFactory.keyStorePath = getClass().getResource("/ca/jetty.jks").getPath()
//		sslcontextFactory.keyManagerPassword = "jetty"
//		sslcontextFactory.keyStorePassword = "jettyjetty"
//		sslcontextFactory.needClientAuth = needClientAuth
//		
//		def https = new ServerConnector(server,
//			new SslConnectionFactory(sslcontextFactory, "http/1.1"),
//			new HttpConnectionFactory())
//		https.setPort(8443);
//		
//		server.addConnector(https)
//		
//		server
//	}
//	
//	def httpsClient(options) {
//		DefaultConversionService service = new DefaultConversionService()
//		GroovyHttpsClient client = new GroovyHttpsClient(Executors.newFixedThreadPool(1), service);
//		
//		
//		
//		def request = client.request (options , { response ->
//			
//			def body = ""
//			response.on "data", { chunck ->
//				body += chunck
//			}
//			
//			response.on "end", {
//				print "end"
//				println response.statusCode
//				println response.headers
//			}
//		})
//		request.on("error", { message ->
//			print message
//		})
//		request.end()
//	}
//	
//	def jsHttpsClient(scriptname) {
//		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
//		String scriptString = IOUtils.toString(TestHttpsClient.class.getResourceAsStream("/be/icode/hot/test/nio/js/${scriptname}"));
//		Script<org.mozilla.javascript.Script> script = new Script<>(scriptString.getBytes(), "httpClient");
//		DefaultConversionService service = new DefaultConversionService()
//		JshttpsClient  client = new JshttpsClient(Executors.newFixedThreadPool(1), service, jsScriptExecutor.globalScope)
//		jsScriptExecutor.execute(script, [client:client])
//	}
//	
//	def pyHttpsClient(scriptname) {
//		PythonScriptExecutor pyScriptExecutor = new PythonScriptExecutor();
//		String scriptString = IOUtils.toString(TestHttpsClient.class.getResourceAsStream("/be/icode/hot/test/nio/python/${scriptname}"));
//		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "httpClient");
//		DefaultConversionService service = new DefaultConversionService()
//		PythonHttpsClient  client = new PythonHttpsClient(Executors.newFixedThreadPool(1), service)
//		pyScriptExecutor.execute(script, [client:client])
//	}
//	
//	def pyHttpClient(scriptname) {
//		PythonScriptExecutor pyScriptExecutor = new PythonScriptExecutor();
//		String scriptString = IOUtils.toString(TestHttpsClient.class.getResourceAsStream("/be/icode/hot/test/nio/python/${scriptname}"));
//		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "httpClient");
//		DefaultConversionService service = new DefaultConversionService()
//		PythonHttpClient  client = new PythonHttpClient(Executors.newFixedThreadPool(1), service)
//		pyScriptExecutor.execute(script, [client:client])
//	}
//	
//	@Test
//	public void testSelfSignedServer() {
//		def options = [
//			hostname: 'localhost',
//			port: 8443,
//			path: "/test",
//			rejectUnauthorized:false,
//			headers:[
//				Authorization: "Basic " + "tomcat:tomcat".bytes.encodeBase64().toString()
//			]
//		]
//		
//		Server server = createHttpsServer (false)
//		server.start()
//		
//		httpsClient(options);
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testSignedServerJKS() {
//		def options = [
//			hostname: 'localhost',
//			port: 8443,
//			path: "/test",
//			ca:"ca/ca.crt",
//			headers:[
//				Authorization: "Basic " + "tomcat:tomcat".bytes.encodeBase64().toString()
//			]
//		]
//		
//		Server server = createHttpsServer (false)
//		server.start()
//		
//		httpsClient(options);
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testSignedClientServerJKS() {
//		def options = [
//			hostname: 'localhost',
//			port: 8443,
//			path: "/test",
//			ca:"ca/ca.crt",
//			jks:"ca/client.jks",
//			jksPassword:"clientclient",
//			jksCertificatePassword:"client",
//			headers:[
//				Authorization: "Basic " + "tomcat:tomcat".bytes.encodeBase64().toString()
//			]
//		]
//		
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		httpsClient(options);
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testSignedClientServerPkcs12() {
//		def options = [
//			hostname: 'localhost',
//			port: 8443,
//			path: "/test",
//			ca:"ca/ca.crt",
//			p12:"ca/client.p12",
//			passphrase:"client",
//			headers:[
//				Authorization: "Basic " + "tomcat:tomcat".bytes.encodeBase64().toString()
//			]
//		]
//		
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		httpsClient(options);
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testSignedClientServerKeyCert() {
//		def options = [
//			hostname: 'localhost',
//			port: 8443,
//			path: "/test",
//			ca:"ca/ca.crt",
//			key:"ca/client.key",
//			cert:"ca/client.crt",
//			passphrase:"client",
//			headers:[
//				Authorization: "Basic " + "tomcat:tomcat".bytes.encodeBase64().toString()
//			]
//		]
//		
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		httpsClient(options);
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	void testJSSelfSignedServer() {
//		Server server = createHttpsServer (false)
//		server.start()
//		
//		jsHttpsClient("https-client-selfsigned.js");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testJSSignedServerJKS() {
//		Server server = createHttpsServer (false)
//		server.start()
//		
//		jsHttpsClient("https-client-jks.js");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testJSSignedClientServerJKS() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		jsHttpsClient("https-client-server-jks.js");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testJSSignedClientServerPkcs12() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		jsHttpsClient("https-client-server-pkcs12.js");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	public void testJSSignedClientServerKeyCert() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		jsHttpsClient("https-client-server-keycert.js");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	void testPythonSelfSignedServer() {
//		Server server = createHttpsServer (false)
//		server.start()
//		
//		pyHttpsClient("https-client-selfsigned.py");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	void testPythonSignedServerJKS() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		pyHttpsClient("https-client-server-jks.py");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	void testPythonSignedServerKeyCert() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		pyHttpsClient("https-client-server-keycert.py");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//	
//	@Test
//	void testPythonSignedServerPkcs12() {
//		Server server = createHttpsServer (true)
//		server.start()
//		
//		pyHttpsClient("https-client-server-pkcs12.py");
//		Thread.currentThread().sleep(3000);
//		server.stop();
//	}
//}
