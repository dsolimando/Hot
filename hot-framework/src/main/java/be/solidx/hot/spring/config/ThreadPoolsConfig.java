package be.solidx.hot.spring.config;

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

import be.solidx.hot.utils.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Configuration
@Import({CommonConfig.class})
public class ThreadPoolsConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolsConfig.class);
	
	private int blockingTasksThreadPoolSize = Runtime.getRuntime().availableProcessors()*2;
	
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private static final int EVENT_LOOP_BACK_LOG_SIZE = 2024;

	@Autowired
	CommonConfig commonConfig;
	
	@Bean(name="blockingTasksThreadPool")
	public ExecutorService blockingTasksThreadPool () throws IOException {
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() == 0)
			return executorService(blockingTasksThreadPoolSize);
		else 
			return new DelegatingSecurityContextExecutorService(executorService(blockingTasksThreadPoolSize));
	}
	
	@Bean(name="taskManagerExecutorService")
	public ScheduledExecutorService taskManagerExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}
	
	@Bean
	public EventLoopFactory eventLoopFactory() throws Exception {
		return new EventLoopFactory(commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0);
	}
	
	@Bean(name="staticResourcesEventLoop")
	public ExecutorService staticResourcesEventLoop() throws Exception {
		ExecutorService rbate = ThreadPoolsConfig.singleThreadExecutorService();
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0) {
			return new DelegatingSecurityContextExecutorService(rbate);
		}
		return rbate;
	}
	
	@Bean(name="httpIOEventLoop")
	public ExecutorService httpIOEventLoop() throws Exception {
        ExecutorService rbate = singleThreadExecutorService();
		if (commonConfig != null && commonConfig.hotConfig().getAuthList().size() > 0) {
			return new DelegatingSecurityContextExecutorService(rbate);
		}
		return rbate;
	}

	public static ExecutorService executorService(int poolSize) {
	    return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(EVENT_LOOP_BACK_LOG_SIZE)
        );
    }

    static public ExecutorService singleThreadExecutorService() {
	    return executorService(1);
    }

	@Bean
	public FileLoader fileLoader() throws Exception {
		return new FileLoader(staticResourcesEventLoop());
	}
	
	public static class EventLoopFactory {
		
		private List<ExecutorService> executorServices = new ArrayList<>();
		
		private int index = 0;
		
		public EventLoopFactory(boolean authEnabled) throws Exception {
			for (int i = 0; i < AVAILABLE_PROCESSORS; i++) {
				ExecutorService rbate = ThreadPoolsConfig.singleThreadExecutorService();

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
