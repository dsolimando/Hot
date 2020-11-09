package be.solidx.hot.test.nio.http

import be.solidx.hot.DataConverter
import be.solidx.hot.Script
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.js.JsMapConverter
import be.solidx.hot.nio.http.*
import be.solidx.hot.python.PyDictionaryConverter
import be.solidx.hot.python.PythonScriptExecutor
import be.solidx.hot.spring.config.ThreadPoolsConfig
import be.solidx.hot.test.promises.TestPromises
import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import org.apache.commons.io.IOUtils
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

import javax.script.CompiledScript
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.util.concurrent.Executors;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

class TesthttpClient {

    NioClientSocketChannelFactory cf = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS)
    SSLContextBuilder sSLContextBuilder = new SSLContextBuilder()
    ObjectMapper objectMapper = new ObjectMapper()
    FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter()
    XStream xStream = new XStream()
    DataConverter dataConverter = new DataConverter()
    JsMapConverter jsMapConverter = new JsMapConverter();
    PyDictionaryConverter pyDictionaryConverter = new PyDictionaryConverter()
    HttpDataSerializer httpDataSerializer = new HttpDataSerializer(formHttpMessageConverter, objectMapper, xStream, dataConverter, jsMapConverter, pyDictionaryConverter)

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
        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url    : 'http://localhost:8080/test',
            type   : 'GET',
            headers: [
                'Content-type': "application/x-www-form-urlencoded"
            ],
            data   : [
                name: 'Damien',
                age : 8
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url    : 'http://localhost:8080/test',
            type   : 'POST',
            headers: [
                'Content-type': "application/json"
            ],
            data   : [
                name: 'Damien',
                age : 8
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url        : 'http://localhost:8080/test',
            type       : 'POST',
            headers    : [
                'Content-type': "application/json"
            ],
            data       : '{"name":"Damien","age":8}',
            processData: false
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url    : 'http://localhost:8080/test',
            type   : 'POST',
            headers: [
                'Content-type': "application/xml"
            ],
            data   : [
                name: 'Damien',
                age : 8
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url        : 'http://localhost:8080/test',
            type       : 'POST',
            headers    : [
                'Content-type': "application/xml"
            ],
            data       : '<root><name>Damien</name><age>8</age></root>',
            processData: false
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url    : 'http://localhost:8080/test',
            type   : 'POST',
            headers: [
                'Content-type': "application/x-www-form-urlencoded"
            ],
            data   : [
                name: 'Damien',
                age : 8
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
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

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url        : 'http://localhost:8080/test',
            type       : 'POST',
            headers    : [
                'Content-type': "application/x-www-form-urlencoded"
            ],
            data       : 'name=Damien&age=8',
            processData: false
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
            assert data == 'name=Damien&age=8'
            server.stop();
        }).fail({ response, status, error ->
            println status
            server.stop();
        })
        server.threadPool.join()
    }

    @Test
    void testGroovyHttpsClientPkcs12ClientXmlServlet() {
        def server = createHttpsServer(true)

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"
        server.setHandler(servletContextHandler)

        ServletHolder holder = new ServletHolder(XmlServlet.class)
        holder.name = "xmlServlet"
        servletContextHandler.addServlet(holder, "/test")

        server.start()

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url: 'https://localhost:8443/test',
            ssl: [
                ca        : 'ca/ca.crt',
                p12       : "ca/client.p12",
                passphrase: "client"
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
            print data.@attr
            server.stop();
        }).fail({ response, status, error ->
            response.printStackTrace()
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
    void testGroovyHttpsClientPkcs12ClientJSONServlet() {
        def server = createHttpsServer(true)

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"
        server.setHandler(servletContextHandler)

        ServletHolder holder = new ServletHolder(JsonServlet.class)
        holder.name = "xmlServlet"
        servletContextHandler.addServlet(holder, "/test")

        server.start()

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url: 'https://localhost:8443/test',
            ssl: [
                ca        : 'ca/ca.crt',
                p12       : "ca/client.p12",
                passphrase: "client"
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
            print data.name
            server.stop();
        }).fail({ response, status, error ->
            response.printStackTrace()
            server.stop();
        })
        server.threadPool.join()
    }

    @Test
    void testGroovyHttpsClientPkcs12ClientJSONServletNoProcess() {
        def server = createHttpsServer(true)

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"
        server.setHandler(servletContextHandler)

        ServletHolder holder = new ServletHolder(JsonServlet.class)
        holder.name = "xmlServlet"
        servletContextHandler.addServlet(holder, "/test")

        server.start()

        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url: 'https://localhost:8443/test',
            ssl: [
                ca             : 'ca/ca.crt',
                p12            : "ca/client.p12",
                passphrase     : "client",
                processResponse: false
            ]
        ]
        client.buildRequest(options).done({ data, textStatus, response ->
            print data
            server.stop();
        }).fail({ response, status, error ->
            println response.message
            server.stop();
        })
        server.threadPool.join()
    }

    def runJsHttpsClient = { scriptname, server ->
        JSScriptExecutor jsScriptExecutor = new JSScriptExecutor()
        DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        def params = [
            client: new JsHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer, dbuilder, jsScriptExecutor.globalScope, new JsMapConverter()),
            server: server]
        String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/test/nio/http/js/${scriptname}"));
        Script<Scriptable> script = new Script<Scriptable>(scriptString.getBytes(), scriptname);
        jsScriptExecutor.execute(script, params);
    }

    @Test
    void testJSHttpClientGET() {
        Server server = server(GetServlet.class)
        server.start()
        runJsHttpsClient "http-client-get.js", server
        server.threadPool.join();
    }

    @Test
    void testJSHttpClientPOSTForm() {
        Server server = server(GetServlet.class)
        server.start()
        runJsHttpsClient("http-client-post-form.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpClientPOSTJson() {
        Server server = server(EchoPOSTServlet.class)
        server.start()
        runJsHttpsClient("http-client-post-json.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpClientPOSTXML() {
        Server server = server(EchoPOSTServlet.class)
        server.start()
        runJsHttpsClient("http-client-post-xml.js", server)
        server.threadPool.join()
    }

    @Test
    void testJSHttpsClientSelfSigned() {
        def server = createHttpsServer(false)
        server.start()
        runJsHttpsClient("https-client-selfsigned.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpsClientJKS() {
        def server = createHttpsServer(false)
        server.start()
        runJsHttpsClient("https-client-jks.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpsClientJKSClientAuth() {
        def server = createHttpsServer(false)
        server.start()
        runJsHttpsClient("https-client-server-jks.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpsClientKeycertClientAuth() {
        def server = createHttpsServer(true)
        server.start()
        runJsHttpsClient("https-client-server-jks.js", server)
        server.threadPool.join();
    }

    @Test
    void testJSHttpsClientPkcs12ClientAuth() {
        def server = createHttpsServer(true)
        server.start()

        runJsHttpsClient("https-client-server-pkcs12.js", server)

        server.threadPool.join();
    }

//    @Test
//    void testJSHttpsClientPkcs12ClientAuthFail() {
//        def server = createHttpsServer(true)
//        server.start()
//
//        runJsHttpsClient("https-client-jks.js", server)
//
//        server.threadPool.join();
//    }

    @Test
    void testJSHttpsClientPkcs12ClientXmlServlet() {
        def server = createHttpsServer(true)

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"
        server.setHandler(servletContextHandler)

        ServletHolder holder = new ServletHolder(XmlServlet.class)
        holder.name = "xmlServlet"
        servletContextHandler.addServlet(holder, "/test")

        server.start()

        runJsHttpsClient("https-client-server-pkcs12-xml-servlet.js", server)

        server.threadPool.join();
    }

    @Test
    void testJSHttpsClientPkcs12ClientJSONServlet() {
        def server = createHttpsServer(true)

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"
        server.setHandler(servletContextHandler)

        ServletHolder holder = new ServletHolder(JsonServlet.class)
        holder.name = "xmlServlet"
        servletContextHandler.addServlet(holder, "/test")

        server.start()

        runJsHttpsClient("https-client-server-pkcs12-json-servlet.js", server)

        server.threadPool.join();
    }

    def runPyHttpsClient = { scriptname, server ->
        PythonScriptExecutor pyScriptExecutor = new PythonScriptExecutor()
        DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        DocumentBuilderFactory f = new DocumentBuilderFactoryImpl()
        f.newDocumentBuilder()
        def params = [
            client: new PythonHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer, f.newDocumentBuilder(), new PyDictionaryConverter()),
            server: server
        ]
        String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/test/nio/http/py/${scriptname}"));
        Script<CompiledScript> script = new Script<CompiledScript>(scriptString.getBytes(), scriptname);
        pyScriptExecutor.execute(script, params);
    }

    @Test
    void testPyHttpClientGET() {
        Server server = server(GetServlet.class)
        server.start()

        runPyHttpsClient 'http-client-get.py', server

        server.threadPool.join();
    }

    @Test
    void testPyHttpClientPOSTForm() {
        Server server = server(EchoPOSTServlet.class)
        server.start()

        runPyHttpsClient 'http-client-post-form.py', server

        server.threadPool.join();
    }

    @Test
    void testPyHttpClientPOSTJson() {
        Server server = server(EchoPOSTServlet.class)
        server.start()

        runPyHttpsClient 'http-client-post-json.py', server

        server.threadPool.join();
    }

    @Test
    void testPyHttpClientPOSTXml() {
        Server server = server(EchoPOSTServlet.class)
        server.start()

        runPyHttpsClient 'http-client-post-xml.py', server

        server.threadPool.join();
    }
}
