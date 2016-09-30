package be.solidx.hot.shows;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;

import reactor.core.Reactor;
import be.solidx.hot.data.DB;
import be.solidx.hot.groovy.GroovyScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.nio.http.GroovyHttpClient;
import be.solidx.hot.nio.http.HttpDataSerializer;
import be.solidx.hot.nio.http.JsHttpClient;
import be.solidx.hot.nio.http.PythonHttpClient;
import be.solidx.hot.nio.http.SSLContextBuilder;
import be.solidx.hot.python.PyDictionaryConverter;
import be.solidx.hot.python.PythonScriptExecutor;
import be.solidx.hot.shows.groovy.GroovyShow;
import be.solidx.hot.shows.javascript.JSShow;
import be.solidx.hot.shows.python.PythonShow;
import be.solidx.hot.spring.config.ThreadPoolsConfig.EventLoopFactory;
import be.solidx.hot.spring.config.event.ReloadShowEvent;
import be.solidx.hot.spring.config.event.RestRegistrationEvent;
import be.solidx.hot.spring.config.event.WebSocketActivationEvent;
import be.solidx.hot.spring.config.event.RestRegistrationEvent.Action;

public class ShowsContext implements  ApplicationEventPublisherAware, ApplicationListener<ReloadShowEvent> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShowsContext.class);

	private List<Show<?,?>> shows = new ArrayList<Show<?,?>>();
	
	private ApplicationContext resourceLoader;
	
	private EventLoopFactory eventLoopFactory;
	
	private ExecutorService blockingThreadPool;
	
	private String defaultShowSearchPath = "**";
	
	GroovyScriptExecutor groovyScriptExecutor;
	
	JSScriptExecutor jsScriptExecutor;
	
	PythonScriptExecutor pythonScriptExecutor;
	
	ScheduledExecutorService taskManager;
	
	Reactor reactor;
	
	Map<String, DB<Map<String, Object>>> groovyDbMap;
	
	Map<String, DB<NativeObject>> jsDbMap;
	
	Map<String, DB<PyDictionary>> pythonDbMap;
	
	ApplicationEventPublisher applicationEventPublisher;
	
	ObjectMapper objectMapper;
	
	HttpDataSerializer httpDataSerializer;
	
	JsMapConverter jsMapConverter;
	
	PyDictionaryConverter pyDictionaryConverter;
	
	SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
	
	Map<ExecutorService, GroovyHttpClient> groovyHttpClientMap = new ConcurrentHashMap<>();
	
	Map<ExecutorService, JsHttpClient> jsHttpClientMap = new ConcurrentHashMap<>();
	
	Map<ExecutorService, PythonHttpClient> pyHttpClientMap = new ConcurrentHashMap<>();
	
	NioClientSocketChannelFactory nioClientSocketChannelFactory;
	
	public ShowsContext(
			ApplicationContext resourceLoader, 
			EventLoopFactory eventLoopFactory, 
			ExecutorService	blockingThreadPool,
			GroovyScriptExecutor groovyScriptExecutor,
			JSScriptExecutor jsScriptExecutor,
			PythonScriptExecutor pythonScriptExecutor,
			ScheduledExecutorService taskManager,
			Reactor reactor,
			Map<String, DB<Map<String, Object>>> groovyDbMap,
			Map<String, DB<NativeObject>> jsDbMap, 
			Map<String, DB<PyDictionary>> pythonDbMap,
			NioClientSocketChannelFactory nioClientSocketChannelFactory, 
			ObjectMapper objectMapper,
			HttpDataSerializer httpDataSerializer,
			JsMapConverter jsMapConverter,
			PyDictionaryConverter pyDictionaryConverter) throws IOException {
		this.resourceLoader = resourceLoader;
		this.eventLoopFactory = eventLoopFactory;
		this.blockingThreadPool = blockingThreadPool;
		
		this.groovyScriptExecutor = groovyScriptExecutor;
		this.pythonScriptExecutor = pythonScriptExecutor;
		this.jsScriptExecutor = jsScriptExecutor;
		this.taskManager = taskManager;
		this.reactor = reactor;
		this.groovyDbMap = groovyDbMap;
		this.jsDbMap = jsDbMap;
		this.pythonDbMap = pythonDbMap;
		this.httpDataSerializer = httpDataSerializer;
		this.objectMapper = objectMapper;
		this.jsMapConverter = jsMapConverter;
		this.pyDictionaryConverter = pyDictionaryConverter;
		this.nioClientSocketChannelFactory = nioClientSocketChannelFactory;
	}

	public void loadShows () {
		shows.clear();
		Resource[] resources;
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Looking for shows in "+"classpath:"+defaultShowSearchPath+"/*.show.*");
			}
			resources = resourceLoader.getResources("classpath*:"+defaultShowSearchPath+"/*.show.*");
		} catch (IOException e) {
			LOGGER.error("Failed to scan classpath for shows");
			return;
		}
		
		for (Resource resource : resources) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Found show "+resource.getURL().getPath());
				}
					
				Show<?, ?> show = createShow(resource.getURL());
					
				if (show != null) {
					shows.add(show);
				}
			} catch (IOException e) {
				LOGGER.error("",e);
			}
		}
	}
	
	public List<Show<?,?>> getShows() {
		return shows;
	}
	
	public void setDefaultShowSearchPath(String defaultShowSearchPath) {
		this.defaultShowSearchPath = defaultShowSearchPath;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onApplicationEvent(ReloadShowEvent event) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Show "+event.getShowUrl().toString()+" changed ["+event.getReloadReason()+"]");
		}
		
		Show<?, ?> show = lookupShow(event.getShowUrl());
		
		if (show == null) {
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Show file not found");
		}
		
		switch (event.getReloadReason()) {
		case ADDED:
			if (show == null) {
				show = createShow(event.getShowUrl());
				if (show != null) {
					shows.add(show);
				}
				applicationEventPublisher.publishEvent(new RestRegistrationEvent(this,show,Action.CREATE));
				applicationEventPublisher.publishEvent(new WebSocketActivationEvent(this, ((AbstractWebSocket)show.getWebsocket()).getSocketHandlerAdapterMap()));
			} else {
				Map webSockethandlersToAddMap = ((AbstractShow)show).reset();
				applicationEventPublisher.publishEvent(new WebSocketActivationEvent(this, webSockethandlersToAddMap));
				applicationEventPublisher.publishEvent(new RestRegistrationEvent(this,show,Action.UPDATE));
			}
			break;
		case DELETED:
			if (show == null) 
				break;
			((AbstractShow)show).close();
			shows.remove(show);
			applicationEventPublisher.publishEvent(new RestRegistrationEvent(this,show,Action.REMOVE));
			break;
			
		case MODIFIED:
			if (show == null)
				break;
			
			Map webSockethandlersToAddMap = ((AbstractShow)show).reset();
			applicationEventPublisher.publishEvent(new WebSocketActivationEvent(this, webSockethandlersToAddMap));
			applicationEventPublisher.publishEvent(new RestRegistrationEvent(this,show,Action.UPDATE));
			break;
		default:
			break;
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	private Show<?, ?> lookupShow (URL url) {
		for (Show<?, ?> show : shows) {
			AbstractShow<?, ?, ?> abstractShow = (AbstractShow<?, ?, ?>) show;
			if (abstractShow.getFilepath().equals(url)) {
				return show;
			}
		}
		return null;
	}
	
	private Show<?, ?> createShow (URL showUrl) {
		AbstractShow<?,?,?> show = null;
		String filename = showUrl.getFile();
		
		try {
		if (filename.endsWith("groovy")) {
				ExecutorService eventLoop = eventLoopFactory.eventLoop();
				GroovyHttpClient groovyHttpClient = groovyHttpClientMap.get(eventLoop);
				if (groovyHttpClient == null) {
					groovyHttpClient = new GroovyHttpClient(eventLoop, nioClientSocketChannelFactory, sslContextBuilder, objectMapper, httpDataSerializer);
					groovyHttpClientMap.put(eventLoop, groovyHttpClient);
				}
				show = new GroovyShow(showUrl, eventLoop, blockingThreadPool, groovyHttpClient, groovyScriptExecutor, taskManager, reactor, groovyDbMap);
			} else if (filename.endsWith("js")) {
				ExecutorService eventLoop = eventLoopFactory.eventLoop();
				JsHttpClient jsHttpClient = jsHttpClientMap.get(eventLoop);
				if (jsHttpClient == null) {
					jsHttpClient = new JsHttpClient(
							eventLoop,
							nioClientSocketChannelFactory, 
							sslContextBuilder, 
							objectMapper, 
							httpDataSerializer, 
							DocumentBuilderFactory.newInstance().newDocumentBuilder(), 
							jsScriptExecutor.getGlobalScope(), 
							jsMapConverter);
					jsHttpClientMap.put(eventLoop, jsHttpClient);
				}
				show = new JSShow(showUrl,eventLoopFactory.eventLoop(), blockingThreadPool, jsHttpClient,  jsScriptExecutor, taskManager, reactor, jsDbMap);
			} else if (filename.endsWith("py")) {
				ExecutorService eventLoop = eventLoopFactory.eventLoop();
				PythonHttpClient pythonHttpClient = pyHttpClientMap.get(eventLoop);
				if (pythonHttpClient == null) {
					pythonHttpClient = new PythonHttpClient(
							eventLoop,
							nioClientSocketChannelFactory, 
							sslContextBuilder, 
							objectMapper, 
							httpDataSerializer, 
							DocumentBuilderFactory.newInstance().newDocumentBuilder(), 
							pyDictionaryConverter);
				}
				show = new PythonShow(showUrl, eventLoopFactory.eventLoop(),blockingThreadPool, pythonHttpClient, pythonScriptExecutor, taskManager, reactor, pythonDbMap);
			}
		} catch (IOException | ParserConfigurationException  e) {
			LOGGER.error(String.format("Failed to load show [%s]", filename));
		}
		return show;
	}
}
