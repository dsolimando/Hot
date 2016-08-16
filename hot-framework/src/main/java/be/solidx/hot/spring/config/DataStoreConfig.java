package be.solidx.hot.spring.config;

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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.solidx.hot.data.DB;
import be.solidx.hot.data.rest.RestDataStore;
import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.spring.config.HotConfig.DataSource;

@Configuration 
@EnableWebMvc
public class DataStoreConfig {

	@Autowired
	DataConfig dataConfig;
	
	@Autowired
	HotConfig hotConfig;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	GroovyMapConverter groovyDataConverter;
	
	@Autowired
	ThreadPoolsConfig threadPoolsConfig;
	
	@Bean
	public ConversionService conversionService() {
		ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
		conversionServiceFactoryBean.afterPropertiesSet();
		return conversionServiceFactoryBean.getObject();
	}
	
	@Bean
	public RestDataStore restDataStore () throws Exception {
		RestDataStore restDataStore = new RestDataStore(restDbMap(), conversionService(),groovyDataConverter, threadPoolsConfig.blockingTasksThreadPool());
		restDataStore.setObjectMapper(objectMapper);
		return restDataStore;
	}
	
	@Bean
	public Map<String, DB<Map<String, Object>>> restDbMap () throws Exception {
		Map<String, DB<Map<String, Object>>> dbMap = dataConfig.groovyDbMap();
		Map<String, DB<Map<String, Object>>> restDbMap = new LinkedHashMap<String, DB<Map<String, Object>>>();
		for (DataSource dataSource : hotConfig.getDataSources()) {
			if (dataSource.isRest()) {
				restDbMap.put(dataSource.getName(), dbMap.get(dataSource.getName()));
			}
		}
		return restDbMap;
	}
}
