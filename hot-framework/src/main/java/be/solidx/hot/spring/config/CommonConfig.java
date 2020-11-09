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

import be.solidx.hot.DataConverter;
import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.python.PyDictionaryConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().with(MapperFeature.USE_STATIC_TYPING);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.getSerializationConfig().with(MapperFeature.USE_ANNOTATIONS);
        objectMapper.configOverride(Map.class).setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
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
	
	@Bean(name="secureDirs")
	public List<String> secureDirs () throws IOException {
		
		Resource[] secureMarkers;
		boolean appServer = false;
		URL configFileURL = configFileURL();
		
		// Application server
		if (configFileURL().getPath().contains("WEB-INF/classes")) {
			secureMarkers = applicationContext.getResources("/WEB-INF/classes/**/.secure");
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
					
					if (LOGGER.isInfoEnabled()) 
						LOGGER.info("Adding secure path "+securepath);
					
					paths.add(securepath);
				} catch (Exception e) {
					paths.add("/"+path.split(getClass().getClassLoader().getResource(".").getPath())[1].replaceAll("/.secure", ""));
				}
			}
		}
		return paths;
	}
	
	@Bean
	public List<String> securityBypassDirs () throws IOException {
		
		URL configFileURL = configFileURL();
		boolean appServer = false;
		
		List<String> secureDirs = secureDirs ();
		List<String> paths = new ArrayList<>();
		String path = null;
		String projectDir = configFileURL.getPath().substring(0, configFileURL.getPath().lastIndexOf("/"));
		
		Resource[] projectResources;
		// Application server
		if (configFileURL().getPath().contains("WEB-INF/classes")) {
			projectResources = applicationContext.getResources("classpath:**");
			appServer = true;
		} else {
			projectResources = applicationContext.getResources("classpath*:/www/**");
		}
		
		for (Resource resource : projectResources) {
			
			if (!resource.getFile().isDirectory()) continue;
			
			path = resource.getURL().getPath();
			
			if (appServer) {
				path = path.split("WEB-INF/classes")[1];
			} else {
				path = path.split(projectDir+"/www")[1];
			}
			
			path = path.substring(0, path.length()-1);
			
			if (path.isEmpty())
				path = "/";
			
			if (!secureDirs.contains(path)) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Path "+path +" will bypass security filter chain");
				}
				paths.add(path);
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
	public Boolean jboss() {
		try {
			boolean isJboss = configFileURL().toURI().getScheme().equalsIgnoreCase("vfs");
			if (isJboss) {
				LOGGER.info("We are running on e JBOSS application server");
			}
			return isJboss;
		} catch (URISyntaxException e) {
			LOGGER.error("",e);
			return false;
		}
	}
	
	@Bean
	public HotConfig hotConfig () throws IOException {
		ObjectMapper objectMapper = objectMapper();
        return objectMapper.readValue(configFileURL().openStream(), HotConfig.class);
	}

	@Bean
	public DiskFileItemFactory diskFileItemFactory() {
	    DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
	    diskFileItemFactory.setRepository(new File(System.getProperty("java.io.tmpdir")));
	    return diskFileItemFactory;
    }
}
