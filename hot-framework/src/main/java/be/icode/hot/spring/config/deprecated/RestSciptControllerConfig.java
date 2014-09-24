//package be.icode.hot.spring.config.deprecated;
//
//import java.io.IOException;
//
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//
//import be.icode.hot.rest.RestConfigLoader;
//import be.icode.hot.rest.RestConfigLoaderIfc;
//import be.icode.hot.rest.RestScriptController;
//import be.icode.hot.rest.URLResolver;
//import be.icode.hot.spring.config.DataConfig;
//import be.icode.hot.spring.config.TranspilersConfig;
//
////@Configuration
////@EnableWebMvc
//@Deprecated
//public class RestSciptControllerConfig {
//	
//	@Autowired
//	TranspilersConfig scriptConfig;
//	
//	@Autowired
//	DataConfig dataConfig;
//	
//	@Autowired
//	ApplicationContext applicationContext;
//	
//	@Autowired
//	ObjectMapper objectMapper;
//	
//	@Bean
//	public RestConfigLoaderIfc restConfigLoader() throws JsonParseException, JsonMappingException, IOException {
//		RestConfigLoader restConfigLoader = new RestConfigLoader();
//		restConfigLoader.setObjectMapper(objectMapper);
//		restConfigLoader.setResourceLoader(applicationContext);
//		return restConfigLoader;
//	}
//	
//	@Bean
//	public URLResolver urlResolver() throws JsonParseException, JsonMappingException, IOException {
//		URLResolver urlResolver = new URLResolver();
//		urlResolver.setRestConfigLoader(restConfigLoader());
//		return urlResolver;
//	}
//	
//	@Bean
//	public RestScriptController restScriptController () throws Exception {
//		RestScriptController restScriptController = new RestScriptController();
//		restScriptController.setGroovyRestScriptExecutor(scriptConfig.groovyScriptExecutor());
//		restScriptController.setGroovyDbMap(dataConfig.groovyDbMap());
//		restScriptController.setJsRestScriptInvoker(scriptConfig.jSScriptExecutorWithPreExecuteScripts());
//		restScriptController.setJsDbMap(dataConfig.jsDbMap());
//		restScriptController.setPythonRestScriptExecutor(scriptConfig.pythonScriptExecutorWithPreExecuteScripts());
//		restScriptController.setPyDbMap(dataConfig.pythonDbMap());
//		restScriptController.setUrlResolver(urlResolver());
//		restScriptController.setObjectMapper(objectMapper);
//		return restScriptController;
//	}
//}
