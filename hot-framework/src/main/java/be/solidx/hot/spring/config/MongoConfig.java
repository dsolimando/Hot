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
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.DBObjectMapTransformer;
import be.solidx.hot.data.mongo.DBObjectNativeObjectTransformer;
import be.solidx.hot.data.mongo.DBObjectPyDictionaryTransformer;
import be.solidx.hot.spring.config.HotConfig.DBEngine;
import be.solidx.hot.spring.config.HotConfig.DataSource;

import com.mongodb.Mongo;

@Configuration @Lazy
public class MongoConfig {
	
	private static final Log LOG = LogFactory.getLog(MongoConfig.class);
	
	@Autowired
	HotConfig hotConfig;
	
	@Bean
	public Map<DataSource,Mongo> mongoDBs () {
		Map<DataSource,Mongo> mongos = new LinkedHashMap<HotConfig.DataSource, Mongo>();
		for (DataSource dataSource: hotConfig.getDataSources()) {
			if (dataSource.getEngine() == DBEngine.MONGODB) {
				try {
					mongos.put(dataSource,new Mongo(dataSource.getHostname()));
				} catch (Exception e) {
					LOG.error("",e);
				}
			}
		}
		return mongos;
	}
	
	@Bean
	public Map<String,DB<Map<String, Object>>> groovyMongoDb () {
		Map<String,DB<Map<String, Object>>> dbmap = new LinkedHashMap<String,DB<Map<String,Object>>>();
		for (Entry<DataSource, Mongo> dataSource: mongoDBs().entrySet()) {
			dbmap.put(dataSource.getKey().getName(), new be.solidx.hot.data.mongo.groovy.DB(
					dataSource.getKey().getUsername(), 
					dataSource.getKey().getPassword(), 
					dataSource.getKey().getDatabase(), 
					dataSource.getValue(), 
					new DBObjectMapTransformer()));
			} 
		return dbmap;
	}
	
	@Bean
	public Map<String, DB<NativeObject>> jsMongoDB () {
		Map<String,DB<NativeObject>> dbmap = new LinkedHashMap<String,DB<NativeObject>>();
		for (Entry<DataSource, Mongo> dataSource: mongoDBs().entrySet()) {
			dbmap.put(dataSource.getKey().getName(), new be.solidx.hot.data.mongo.js.DB(					
					dataSource.getKey().getUsername(), 
					dataSource.getKey().getPassword(), 
					dataSource.getKey().getDatabase(), 
					dataSource.getValue(), 
					new DBObjectNativeObjectTransformer()));
		} 
		return dbmap;
	}
	
	@Bean
	public Map<String, DB<PyDictionary>> pyMongoDB () {
		Map<String, DB<PyDictionary>> dbmap = new LinkedHashMap<String, DB<PyDictionary>>();
		for (Entry<DataSource, Mongo> dataSource: mongoDBs().entrySet()) {
			dbmap.put(dataSource.getKey().getName(), new BasicDB<PyDictionary>(					
					dataSource.getKey().getUsername(), 
					dataSource.getKey().getPassword(), 
					dataSource.getKey().getDatabase(), 
					dataSource.getValue(), 
					new DBObjectPyDictionaryTransformer()));
		}
		return dbmap;
	}
}
