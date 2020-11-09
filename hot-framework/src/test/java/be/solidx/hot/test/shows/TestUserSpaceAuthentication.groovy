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
import be.solidx.hot.spring.config.SecurityConfig
import be.solidx.hot.spring.config.ShowConfig
import be.solidx.hot.spring.config.SocialConfig
import be.solidx.hot.spring.config.ThreadPoolsConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy

import javax.servlet.DispatcherType
import java.util.concurrent.Executors

class TestUserSpaceAuthentication {

    NioClientSocketChannelFactory cf = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS)
    SSLContextBuilder sSLContextBuilder = new SSLContextBuilder()
    ObjectMapper objectMapper = new ObjectMapper()
    FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter()
    XStream xStream = new XStream()
    DataConverter dataConverter = new DataConverter()
    JsMapConverter jsMapConverter = new JsMapConverter()
    PyDictionaryConverter pyDictionaryConverter = new PyDictionaryConverter()
    HttpDataSerializer httpDataSerializer = new HttpDataSerializer(formHttpMessageConverter, objectMapper, xStream, dataConverter, jsMapConverter, pyDictionaryConverter)

    Server server;

    def createServer = {
        server = new Server(8080)
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        server.setHandler(servletContextHandler)

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext()
        rootContext.register(ShowConfig.class, SocialConfig.class, SecurityConfig.class)
        ContextLoaderListener listener = new ContextLoaderListener(rootContext)
        servletContextHandler.addEventListener(listener)


        FilterHolder filterHolder = new FilterHolder(DelegatingFilterProxy.class)
        filterHolder.name = "springSecurityFilterChain"
        filterHolder.asyncSupported = true
        servletContextHandler.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.ASYNC, DispatcherType.REQUEST))

        ServletHolder restHolder = new ServletHolder(RestClosureServlet.class)
        restHolder.name = "hot-rest"
        servletContextHandler.addServlet(restHolder, "/rest/*")
    }

    @Before
    void before() {
        System.setProperty("configFileLocation", "be/solidx/hot/test/shows/user-space-secured-config.json")
        createServer()
        server.start()
    }

    @After
    void after() {
        server.threadPool.join()
    }

    @Test
    void test() {
        GroovyHttpClient client = new GroovyHttpClient(Executors.newCachedThreadPool(), cf, sSLContextBuilder, objectMapper, httpDataSerializer)
        def options = [
            url : 'http://localhost:8080/rest/authenticate',
            type: 'GET',
        ]

        def options2 = [
            url : 'http://localhost:8080/rest/lolos',
            type: 'GET'
        ]
        client.buildRequest(options2).then { data, textStatus, response ->
            println response.headers
            assert textStatus == "Full authentication is required to access this resource"
            client.buildRequest(options)
        }.then { data, textStatus, response ->
            println new String(data)
            options2.headers = [
                'Cookie':response.headers['Set-Cookie']
            ]
            client.buildRequest(options2)
        }.then { data, textStatus, response ->
            assert textStatus == "OK"
            server.stop()
        }
    }
}