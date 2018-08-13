package be.solidx.hot.shows.rest;

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

import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.nio.http.HttpDataSerializer;
import be.solidx.hot.python.PyDictionaryConverter;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.utils.GroovyHttpDataDeserializer;
import be.solidx.hot.utils.JsHttpDataDeserializer;
import be.solidx.hot.utils.PythonHttpDataDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class RestClosureServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestClosureServlet.class);
	
	private static final long serialVersionUID = 1534320093731293327L;
	
	protected static final String DEFAULT_CHARSET = "utf-8";
	protected static final String DEFAULT_ACCEPT = MediaType.TEXT_PLAIN.toString();

	ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;
	
	HttpDataSerializer 					httpDataSerializer;
	
	GroovyMapConverter 					groovyDataConverter;
	
	PyDictionaryConverter 				pyDictionaryConverter;
	
	JsMapConverter 						jsDataConverter;
	
	GroovyHttpDataDeserializer 			groovyHttpDataDeserializer;
	
	PythonHttpDataDeserializer 			pythonHttpDataDeserializer;
	
	JsHttpDataDeserializer 				jsHttpDataDeserializer;
	
	ExecutorService						blockingTreadPool;
	
	ExecutorService						httpIOEventLoop;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestClosureDelegate delegate = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()).getBean(RestClosureDelegate.class);
	    delegate.asyncHandleRestRequest(req,resp,req.startAsync());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
