package be.icode.hot.spring.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
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
		Resource[] secureMarkers = applicationContext.getResources("classpath*:**/.secure");
		
		List<String> paths = new ArrayList<>();
		String path = null;
		for (Resource marker : secureMarkers) {
			path = marker.getURL().getPath();
			// Application server case
			if (path.contains("WEB-INF/classes")) {
				paths.add(marker.getURL().getPath().split("WEB-INF/classes")[1].replaceAll("/.secure", ""));
			} else {
				paths.add("/"+marker.getURL().getPath().split(getClass().getClassLoader().getResource(".").getPath())[1].replaceAll("/.secure", ""));
			}
		}
		return paths;
	}
	
	@Bean
	public HotConfig hotConfig () throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getSerializationConfig().enable(Feature.USE_STATIC_TYPING);
		objectMapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
		objectMapper.getSerializationConfig().enable(Feature.USE_ANNOTATIONS);
		objectMapper.getSerializationConfig().disable(Feature.WRITE_NULL_MAP_VALUES);
		
		String configFile = System.getProperty("configFileLocation");
		if (configFile == null) configFile = "config.json";
		try {
			return objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(configFile), HotConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			return objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(".config.js"), HotConfig.class);
		}
	}
}
