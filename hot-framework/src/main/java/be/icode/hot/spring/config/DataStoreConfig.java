package be.icode.hot.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import be.icode.hot.data.DB;
import be.icode.hot.data.rest.RestDataStore;
import be.icode.hot.groovy.GroovyMapConverter;
import be.icode.hot.spring.config.HotConfig.DataSource;

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
