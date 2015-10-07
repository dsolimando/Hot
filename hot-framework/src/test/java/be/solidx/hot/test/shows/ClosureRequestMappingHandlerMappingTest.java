package be.solidx.hot.test.shows;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.solidx.hot.groovy.GroovyScriptExecutor;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.python.PythonScriptExecutor;
import be.solidx.hot.rest.HttpRequest;
import be.solidx.hot.shows.ClosureRequestMapping;
import be.solidx.hot.shows.ShowsContext;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.spring.config.ThreadPoolsConfig;
import be.solidx.hot.spring.config.ThreadPoolsConfig.EventLoopFactory;
import be.solidx.hot.test.shows.ClosureRequestMappingHandlerMappingTest.Config;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {Config.class})
public class ClosureRequestMappingHandlerMappingTest {

	@Autowired
	ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Test
	public void testResolver() throws Exception {
		
		Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
		headers.put("Host", new Vector<>(Arrays.asList("agile.dzone.com")).elements());
		headers.put("Accept",new Vector<>(Arrays.asList("text/html","application/xhtml+xml","application/xml;q=0.9,*/*;q=0.8")).elements());
		headers.put("Accept-Charset", new Vector<>(Arrays.asList("ISO-8859-1","utf-8;q=0.7,*;q=0.7")).elements());
		HttpRequest httpRequest = new HttpRequest(new URL("http://hot.solidx.be:8080/app/repository/items"), "application/json", "GET", headers, "/repository");
		
		ClosureRequestMapping closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(httpRequest);
		ClosureRequestMapping closureRequestMapping2 = closureRequestMappingHandlerMapping.lookupRequestMapping(httpRequest);
		Assert.assertNotNull(closureRequestMapping);
		Assert.assertNotNull(closureRequestMapping2);
		Assert.assertEquals(closureRequestMapping, closureRequestMapping2);
	} 
	
	@Test
	public void testResolver2() throws Exception {
		Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
		headers.put("Host", new Vector<>(Arrays.asList("agile.dzone.com")).elements());
		headers.put("Accept",new Vector<>(Arrays.asList("text/html","application/xhtml+xml","application/xml;q=0.9,*/*;q=0.8")).elements());
		headers.put("Accept-Charset", new Vector<>(Arrays.asList("ISO-8859-1","utf-8;q=0.7,*;q=0.7")).elements());
		HttpRequest httpRequest = new HttpRequest(new URL("http://hot.solidx.be:8080/app/repository/items"), "application/x-www-form-urlencoded", "GET", headers, "/repository");
		
		ClosureRequestMapping closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(httpRequest);
		Assert.assertNotNull(closureRequestMapping);
		Assert.assertTrue(closureRequestMapping.getClosure() instanceof JSClosure);
	} 
	
	
	@Configuration
	@Import({ThreadPoolsConfig.class})
	public static class Config {
		
		@Bean
		public GroovyScriptExecutor groovyScriptExecutor() {
			return new GroovyScriptExecutor();
		}
		
		@Bean
		public PythonScriptExecutor pythonScriptExecutor() {
			return new PythonScriptExecutor();
		}
		
		@Bean
		public JSScriptExecutor jsScriptExecutor() {
			return new JSScriptExecutor();
		}
		
		@Bean
		public ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping(ShowsContext showsContext, ApplicationContext applicationContext) {
			ClosureRequestMappingHandlerMapping handlerMapping = new ClosureRequestMappingHandlerMapping(showsContext);
			handlerMapping.setApplicationContext(applicationContext);
			return handlerMapping;
		}
		
		@Bean
		public ShowsContext ShowsContext(
				ApplicationContext applicationContext,
				EventLoopFactory eventLoopFactory,
				@Named("blockingTasksThreadPool") ExecutorService blockingThreadPool,
				GroovyScriptExecutor groovyScriptExecutor,
				PythonScriptExecutor pythonScriptExecutor,
				JSScriptExecutor jsScriptExecutor) throws IOException {
			ShowsContext ctx = new ShowsContext(applicationContext, 
					eventLoopFactory, 
					blockingThreadPool, 
					groovyScriptExecutor, 
					jsScriptExecutor, 
					pythonScriptExecutor, 
					null, null, null, null, null,null,null,null,null,null);
			
			ctx.setDefaultShowSearchPath("/requestMappingTests");
			ctx.loadShows();
			return ctx;
		}
	}
}
