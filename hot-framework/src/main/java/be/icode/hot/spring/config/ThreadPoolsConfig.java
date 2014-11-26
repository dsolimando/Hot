package be.icode.hot.spring.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import reactor.core.Environment;
import reactor.spring.core.task.RingBufferAsyncTaskExecutor;
import be.icode.hot.utils.FileLoader;

@Configuration
@Import({CommonConfig.class})
public class ThreadPoolsConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolsConfig.class);
	
	private int blockingTasksThreadPoolSize = Runtime.getRuntime().availableProcessors()*2;
	
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	@Autowired
	CommonConfig commonConfig;
	
	@Bean(name="blockingTasksThreadPool")
	public ExecutorService blockingTasksThreadPool () throws JsonParseException, JsonMappingException, IOException {
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() == 0)
			return Executors.newFixedThreadPool(blockingTasksThreadPoolSize);
		else 
			return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(blockingTasksThreadPoolSize));
	}
	
	@Bean(name="taskManagerExecutorService")
	public ScheduledExecutorService taskManagerExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}
	
	@Bean
	public EventLoopFactory eventLoopFactory() throws Exception {
		return new EventLoopFactory(reactorEnvironment(), commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0);
	}
	
	@Bean
	public Environment reactorEnvironment() {
		return new Environment();
	}
	
	@Bean(name="staticResourcesEventLoop")
	public ExecutorService staticResourcesEventLoop() throws Exception {
		RingBufferAsyncTaskExecutor rbate = new RingBufferAsyncTaskExecutor(reactorEnvironment())
	        .setName("httpIOEventLoop")
	        .setBacklog(2048);
		rbate.afterPropertiesSet();
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0) {
			return new DelegatingSecurityContextExecutorService(rbate);
		}
		return rbate;
	}
	
	@Bean(name="httpIOEventLoop")
	public ExecutorService httpIOEventLoop() throws Exception {
		RingBufferAsyncTaskExecutor rbate = new RingBufferAsyncTaskExecutor(reactorEnvironment())
	        .setName("httpIOEventLoop")
	        .setBacklog(2048);
		rbate.afterPropertiesSet();
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0) {
			return new DelegatingSecurityContextExecutorService(rbate);
		}
		return rbate;
	}
	
	@Bean
	public FileLoader fileLoader() throws Exception {
		return new FileLoader(staticResourcesEventLoop());
	}
	
	public static class EventLoopFactory {
		
		private List<ExecutorService> executorServices = new ArrayList<>();
		
		private int index = 0;
		
		public EventLoopFactory(Environment environment, boolean authEnabled) throws Exception {
			for (int i = 0; i < AVAILABLE_PROCESSORS; i++) {
				RingBufferAsyncTaskExecutor rbate = new RingBufferAsyncTaskExecutor(environment)
			        .setName("ringBufferExecutor")
			        .setBacklog(2048);
//			        .setWaitStrategy(new YieldingWaitStrategy());
				rbate.afterPropertiesSet();
				
				if (authEnabled) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Wrapping ExecutorService with DelegatingSecurityContextExecutorService");
					}
					DelegatingSecurityContextExecutorService securityRbate = new DelegatingSecurityContextExecutorService(rbate);
					executorServices.add(securityRbate);
				} else {
					executorServices.add(rbate);
				}
			}
		}
		
		public ExecutorService eventLoop () {
			return executorServices.get((index++) % AVAILABLE_PROCESSORS);
		}			
	}
}
