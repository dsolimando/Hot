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

package be.solidx.hot.test.shows

import be.solidx.hot.DataConverter
import be.solidx.hot.js.JsMapConverter
import be.solidx.hot.nio.http.GroovyHttpClient
import be.solidx.hot.nio.http.HttpDataSerializer
import be.solidx.hot.nio.http.SSLContextBuilder
import be.solidx.hot.python.PyDictionaryConverter
import be.solidx.hot.shows.rest.RestClosureServlet
import be.solidx.hot.spring.config.ShowConfig
import be.solidx.hot.spring.config.ThreadPoolsConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext

import java.util.concurrent.Executors

class TestRESTServices {

	NioClientSocketChannelFactory cf =new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS)
	SSLContextBuilder sSLContextBuilder = new SSLContextBuilder()
    ObjectMapper objectMapper = new ObjectMapper()
	FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter()
	XStream xStream = new XStream()
	DataConverter dataConverter = new DataConverter()
    JsMapConverter jsMapConverter = new JsMapConverter()
    PyDictionaryConverter pyDictionaryConverter = new PyDictionaryConverter()
	HttpDataSerializer httpDataSerializer = new HttpDataSerializer(formHttpMessageConverter, objectMapper, xStream, dataConverter, jsMapConverter, pyDictionaryConverter)
	
	def createServer = {
		Server server = new Server(8087)
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
			url: 'http://localhost:8087/rest/jsitem',
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
    public void testAwait () {
        def server = createServer()
        server.start()
        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
                url: 'http://localhost:8087/rest/asyncawait',
                type: 'GET'
        ]
        client.buildRequest (options).done({ data, textStatus, response ->
            print data
            assert data == '10.0'
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
			url: 'http://localhost:8087/rest/scells/3',
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
			url: 'http://localhost:8087/rest/scells',
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
			url: 'http://localhost:8087/rest/scells',
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
			url: 'http://localhost:8087/rest/scells-slow',
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
			url: 'http://localhost:8087/rest/scells-index.html',
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
    public void testScellsText() {
        def server = createServer()
        server.start()
        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(),cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url: 'http://localhost:8087/rest/scells-txt',
            type: 'GET',
            headers:[
                'Accept':"text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7"
            ],
//			processResponse:false
        ]
        def resp
        client.buildRequest (options).done({ data, textStatus, response ->
            print data
            assert data == 'Hello World'
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
			url: 'http://localhost:8087/rest/scells-response',
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

    @Test
    void testFileUpload() {
        def server = createServer()
        server.start()

        CloseableHttpClient httpclient = HttpClients.createDefault();
        InputStreamBody bin = new InputStreamBody(getClass().getResourceAsStream('/images/chaise.jpg'),ContentType.IMAGE_JPEG,"chaise.jpg");
        StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
        HttpEntity httpEntity = MultipartEntityBuilder.create().addPart('bin',bin).addPart("comment",comment).build()

        HttpPost httppost = new HttpPost("http://localhost:8087/rest/upload");
        httppost.setEntity(httpEntity)
        CloseableHttpResponse response = httpclient.execute(httppost);
        try {
            HttpEntity resEntity = response.getEntity();
            assert '[{"name":"bin","isFile":true,"filename":"chaise.jpg"},{"name":"comment","isFile":false}]' ==  new String(resEntity.content.bytes,"UTF-8")
            def f =  new File('/tmp/chaise.jpg')
            assert f.exists()
            f.delete()
        } finally {
            response.close();
            server.stop()
        }
    }
}
