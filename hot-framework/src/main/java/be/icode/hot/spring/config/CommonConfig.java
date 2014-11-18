package be.icode.hot.spring.config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import be.icode.hot.DataConverter;
import be.icode.hot.groovy.GroovyMapConverter;
import be.icode.hot.js.JsMapConverter;
import be.icode.hot.python.PyDictionaryConverter;

import com.thoughtworks.xstream.XStream;

@Configuration
public class CommonConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonConfig.class);
	
	@Autowired
	ApplicationContext applicationContext;

	@Bean
	public GroovyMapConverter groovyDataConverter () {
		return new GroovyMapConverter();
	}
	
	@Bean
	public PyDictionaryConverter pyDictionaryConverter () {
		return new PyDictionaryConverter();
	}
	
	@Bean
	public JsMapConverter jsMapConverter () {
		return new JsMapConverter();
	}
	
	@Bean
	public ObjectMapper objectMapper() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getSerializationConfig().enable(Feature.USE_STATIC_TYPING);
		if (hotConfig().isDevMode())
			objectMapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
		objectMapper.getSerializationConfig().enable(Feature.USE_ANNOTATIONS);
		objectMapper.getSerializationConfig().disable(Feature.WRITE_NULL_MAP_VALUES);
		return new ObjectMapper();
	}
	
	@Bean
	public XStream xStream () {
		return new XStream();
	}
	
	@Bean
	public DataConverter dataConverter() {
		return new DataConverter();
	}
	
	@Bean
	public List<String> secureDirs () throws IOException {
		
		Resource[] secureMarkers;
		boolean appServer = false;
		URL configFileURL = configFileURL();
		
		// Application server
		if (configFileURL().getPath().contains("WEB-INF/classes")) {
			secureMarkers = applicationContext.getResources("classpath:**/.secure");
			appServer = true;
		} else {
			secureMarkers = applicationContext.getResources("classpath*:/www/**/.secure");
		}
		
		List<String> paths = new ArrayList<>();
		String path = null;
		String projectDir = configFileURL.getPath().substring(0, configFileURL.getPath().lastIndexOf("/"));
		
		for (Resource marker : secureMarkers) {
			path = marker.getURL().getPath();
			
			// Application server case
			if (appServer) {
				paths.add(path.split("WEB-INF/classes")[1].replaceAll("/.secure", ""));
			} 
			else {
				try {
					String securepath = path.split(projectDir+"/www")[1].replaceAll("/.secure", "");
					
					if (securepath.isEmpty()) 
						securepath = "/";
					
					if (LOGGER.isDebugEnabled()) 
						LOGGER.debug("Adding secure path "+securepath);
					
					paths.add(securepath);
				} catch (Exception e) {
					paths.add("/"+path.split(getClass().getClassLoader().getResource(".").getPath())[1].replaceAll("/.secure", ""));
				}
			}
		}
		return paths;
	}
	
	@Bean
	public URL configFileURL() {
		String configFile = System.getProperty("configFileLocation");
		if (configFile == null) configFile = "config.json";
		
		if (!configFile.startsWith("/"))
			configFile = "/"+configFile;
		
		return getClass().getResource(configFile);
	}
	
	@Bean
	public HotConfig hotConfig () throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getSerializationConfig().enable(Feature.USE_STATIC_TYPING);
		objectMapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
		objectMapper.getSerializationConfig().enable(Feature.USE_ANNOTATIONS);
		objectMapper.getSerializationConfig().disable(Feature.WRITE_NULL_MAP_VALUES);
		
		return objectMapper.readValue(configFileURL().openStream(), HotConfig.class);
	}
}
