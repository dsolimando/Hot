//package be.icode.hot.spring.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import be.icode.hot.shows.rest.RestClosureController;
//
//@Configuration
//@EnableWebMvc
//public class RestShowConfig {
//
//	@Autowired
//	ShowConfig showConfig;
//	
//	@Autowired
//	CommonConfig commonConfig;
//	
//	@Autowired
//	ScriptExecutorsConfig scriptExecutorsConfig;
//	
//	@Bean
//	RestClosureController restClosureController() throws Exception {
//		return new RestClosureController(
//				showConfig.closureRequestMappingHandlerMapping(), 
//				showConfig.httpDataSerializer(), 
//				commonConfig.groovyDataConverter(), 
//				commonConfig.pyDictionaryConverter(), 
//				commonConfig.jsMapConverter(), 
//				scriptExecutorsConfig.groovyHttpDataDeserializer(), 
//				scriptExecutorsConfig.pythonHttpDataDeserializer(), 
//				scriptExecutorsConfig.jsHttpDataDeserializer());
//	}
//}
