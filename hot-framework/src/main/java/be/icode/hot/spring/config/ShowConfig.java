package be.icode.hot.spring.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.FormHttpMessageConverter;

import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import be.icode.hot.nio.http.HttpDataSerializer;
import be.icode.hot.shows.ShowsContext;
import be.icode.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.icode.hot.utils.dev.ScriptsWatcher;

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
}
