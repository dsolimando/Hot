package be.solidx.hot.spring.config;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import be.solidx.hot.shows.RestRequestBuilderFactory;
import be.solidx.hot.shows.rest.RestClosureDelegate;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.FormHttpMessageConverter;

import org.xml.sax.SAXException;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import be.solidx.hot.nio.http.HttpDataSerializer;
import be.solidx.hot.shows.ShowsContext;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.utils.dev.ScriptsWatcher;

import javax.xml.parsers.ParserConfigurationException;

@Configuration
@Import({CommonConfig.class,ThreadPoolsConfig.class, ScriptExecutorsConfig.class, DataConfig.class})
public class ShowConfig {
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	CommonConfig commonConfig;
	
	@Autowired
	ThreadPoolsConfig threadPoolsConfig;
	
	@Autowired
	ScriptExecutorsConfig scriptExecutorsConfig;
	
	@Autowired
	DataConfig dataConfig;
	
	@Bean
	public ShowsContext showsContext () throws Exception {
		ShowsContext showsContext = new ShowsContext(
				applicationContext, 
				threadPoolsConfig.eventLoopFactory(),
				threadPoolsConfig.blockingTasksThreadPool(),
				scriptExecutorsConfig.groovyScriptExecutor(),
				scriptExecutorsConfig.jSScriptExecutorWithPreExecuteScripts(),
				scriptExecutorsConfig.pythonScriptExecutorWithPreExecuteScripts(),
				threadPoolsConfig.taskManagerExecutorService(),
				reactor(),
				dataConfig.groovyDbMap(),
				dataConfig.jsDbMap(),
				dataConfig.pythonDbMap(),
				socketChannelFactory(),
				commonConfig.objectMapper(),
				httpDataSerializer(),
				commonConfig.jsMapConverter(),
				commonConfig.pyDictionaryConverter());
		
		showsContext.loadShows();
		return showsContext;
	}
	
	@Bean
	public ScriptsWatcher scriptsWatcher() throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
		if (commonConfig.hotConfig().isDevMode() && System.getProperty("hot.app.dir") != null) {
			final ScriptsWatcher scriptsWatcher = new ScriptsWatcher(new URI("file:"+System.getProperty("hot.app.dir")+"/shows"));
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					scriptsWatcher.watch();
				}
			}).start();
			return scriptsWatcher;
		}
		return null;
	}
	
	@Bean
	public FormHttpMessageConverter formHttpMessageConverter () {
		return new FormHttpMessageConverter();
	}
	
	@Bean
	public HttpDataSerializer httpDataSerializer() throws JsonParseException, JsonMappingException, IOException {
		return new HttpDataSerializer(formHttpMessageConverter(), commonConfig.objectMapper(), commonConfig.xStream(), commonConfig.dataConverter());
	}
	
	@Bean
	public Reactor reactor() {
		return Reactors.reactor().env(threadPoolsConfig.reactorEnvironment()).get();
	}
	
	@Bean
	ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping () throws Exception {
		return new ClosureRequestMappingHandlerMapping(showsContext());
	}
	
	@Bean
	NioClientSocketChannelFactory 	socketChannelFactory() {
		return new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, ThreadPoolsConfig.AVAILABLE_PROCESSORS);
	}

	@Bean
	RestRequestBuilderFactory restRequestBuilderFactory() throws Exception {
	    return new RestRequestBuilderFactory(
                scriptExecutorsConfig.groovyHttpDataDeserializer(),
                scriptExecutorsConfig.pythonHttpDataDeserializer(),
                scriptExecutorsConfig.jsHttpDataDeserializer(),
                commonConfig.groovyDataConverter(),
                commonConfig.pyDictionaryConverter(),
                commonConfig.jsMapConverter()
        );
    }

	@Bean
	RestClosureDelegate restClosureDelegate() throws Exception {
	    return new RestClosureDelegate(
            closureRequestMappingHandlerMapping(),
            httpDataSerializer(),
            commonConfig.groovyDataConverter(),
            commonConfig.pyDictionaryConverter(),
            commonConfig.jsMapConverter(),
            scriptExecutorsConfig.groovyHttpDataDeserializer(),
            scriptExecutorsConfig.pythonHttpDataDeserializer(),
            scriptExecutorsConfig.jsHttpDataDeserializer(),
            threadPoolsConfig.blockingTasksThreadPool(),
            threadPoolsConfig.httpIOEventLoop(),
            restRequestBuilderFactory(),
            commonConfig.diskFileItemFactory()
        );
    }
}
