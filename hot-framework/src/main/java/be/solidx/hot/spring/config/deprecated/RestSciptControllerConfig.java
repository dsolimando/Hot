package be.solidx.hot.spring.config.deprecated;

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
