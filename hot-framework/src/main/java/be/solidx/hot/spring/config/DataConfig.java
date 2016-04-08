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

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import be.solidx.hot.data.DB;
import be.solidx.hot.data.jdbc.DBFactory;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

@Configuration
@Import({CommonConfig.class, MongoConfig.class, ThreadPoolsConfig.class})
public class DataConfig {
	
	private static final Log logger = LogFactory.getLog(DataConfig.class);
	
	private static final String ORACLE_JDBC_URL = "jdbc:oracle:thin:@//%s:%s/%s";
	
	private static final String DB2_JDBC_URL = "jdbc:db2://%s:%s/%s";
	
	private static final String MYSQL_JDBC_URL = "jdbc:mysql://%s:%s/%s";
	
	private static final String PGSQL_JDBC_URL = "jdbc:postgresql://%s:%s/%s";
	
	private static final String HSQLDB_JDBC_URL = "jdbc:hsqldb:file:.db/%s";
	
	@Autowired
	HotConfig hotConfig;
	
	@Autowired
	MongoConfig mongoConfig;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	ThreadPoolsConfig threadPoolsConfig;
	
	@Bean
	public Map<String,DB<Map<String, Object>>> groovyDbMap() throws Exception {
		Map<String,DB<Map<String, Object>>> dbMap = new LinkedHashMap<String, DB<Map<String, Object>>>();
		for (DBFactory dbFactory: dbFactories()) {
			dbMap.put(dbFactory.getName(),dbFactory.buildGroovyDB());
		}
		dbMap.putAll(mongoConfig.groovyMongoDb());
		return dbMap;
	}
	
	@Bean
	public Map<String, DB<NativeObject>> jsDbMap() throws Exception {
		Map<String, DB<NativeObject>> dbMap = new LinkedHashMap<String, DB<NativeObject>>();
		for (DBFactory dbFactory: dbFactories()) {
			dbMap.put(dbFactory.getName(),dbFactory.buildJsDB());
		}
		dbMap.putAll(mongoConfig.jsMongoDB());
		return dbMap;
	}
	
	@Bean
	public Map<String, DB<PyDictionary>> pythonDbMap() throws Exception {
		Map<String, DB<PyDictionary>> dbMap = new LinkedHashMap<String, DB<PyDictionary>>();
		for (DBFactory dbFactory: dbFactories()) {
			dbMap.put(dbFactory.getName(),dbFactory.buildPyDB());
		}
		dbMap.putAll(mongoConfig.pyMongoDB());
		return dbMap;
	}
	
	@Bean
	public List<DBFactory> dbFactories () throws Exception {
		List<DBFactory> dbFactories = new ArrayList<DBFactory>();
		for (Entry<be.solidx.hot.spring.config.HotConfig.DataSource, DataSource> entry : dataSources().entrySet()) {
			DBFactory dbFactory = new DBFactory();
			dbFactory.setDataSource(entry.getValue());
			dbFactory.setEngine(DBEngine.valueOf(entry.getKey().getEngine().name()));
			dbFactory.setName(entry.getKey().getName());
			dbFactory.setSchema(entry.getKey().getSchema());
			dbFactory.init();
			dbFactories.add(dbFactory);
		}
		return dbFactories;
	}
	
	@Bean
	public Map<be.solidx.hot.spring.config.HotConfig.DataSource, DataSource> dataSources () throws Exception {
		Map<be.solidx.hot.spring.config.HotConfig.DataSource, DataSource> dataSources = new HashMap<be.solidx.hot.spring.config.HotConfig.DataSource, DataSource>();
		for (be.solidx.hot.spring.config.HotConfig.DataSource dataSource : hotConfig.getDataSources()) {
			switch (dataSource.getEngine()) {
			case ORACLE:
				dataSources.put(dataSource, oracleSimpleDriverDataSource(dataSource.getHostname(), dataSource.getPort(), dataSource.getDatabase(), dataSource.getUsername(), dataSource.getPassword()));
				break;
			case DB2:
				dataSources.put(dataSource,db2SimpleDriverDataSource(dataSource.getHostname(), dataSource.getPort(), dataSource.getDatabase(), dataSource.getUsername(), dataSource.getPassword()));
				break;
			case MYSQL:
				dataSources.put(dataSource,mysqlSimpleDriverDataSource(dataSource.getHostname(), dataSource.getPort(), dataSource.getDatabase(), dataSource.getUsername(), dataSource.getPassword()));
				break;
			case PGSQL:
				dataSources.put(dataSource,pgsqlSimpleDriverDataSource(dataSource.getHostname(), dataSource.getPort(), dataSource.getDatabase(), dataSource.getUsername(), dataSource.getPassword()));
				break;
			case HSQLDB:
				DataSource sqlDataSource = hsqldbSimpleDriverDataSource(dataSource.getDatabase(), dataSource.getUsername(), dataSource.getPassword());
				try {
					JdbcTemplate jdbcTemplate = new JdbcTemplate(sqlDataSource);
					jdbcTemplate.afterPropertiesSet();
					for (Resource resource : applicationContext.getResources("classpath*:/sql/*-init.sql")) {
						String[] statements = IOUtils.toString(resource.getInputStream()).split(";");
						jdbcTemplate.batchUpdate(statements);
					}
				} catch (Exception e) {
					logger.error("",e);
				}
				dataSources.put(dataSource, sqlDataSource);
				break;
			default:
				break;
			}
		}
		return dataSources;
	}
	
	public DataSource oracleSimpleDriverDataSource (String host, int port, String service, String username, String password) throws Exception {
		Driver oracleDriver = (Driver) Class.forName("oracle.jdbc.OracleDriver").newInstance();
		return new SimpleDriverDataSource(oracleDriver, String.format(ORACLE_JDBC_URL,host,port,service),username,password);
	}
	
	public DataSource db2SimpleDriverDataSource (String host, int port, String dbname, String username, String password) throws Exception {
		Driver db2Driver = (Driver) Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		return new SimpleDriverDataSource(db2Driver, String.format(DB2_JDBC_URL,host,port,dbname),username,password);
	}
	
	public DataSource mysqlSimpleDriverDataSource (String host, int port, String dbname, String username, String password) throws Exception {
		Driver mysqlDriver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
		return new SimpleDriverDataSource(mysqlDriver, String.format(MYSQL_JDBC_URL,host,port,dbname),username,password);
	}
	
	public DataSource pgsqlSimpleDriverDataSource (String host, int port, String dbname, String username, String password) throws Exception {
		Driver pgsqlDriver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
		return new SimpleDriverDataSource(pgsqlDriver, String.format(PGSQL_JDBC_URL,host,port,dbname),username,password);
	}
	
	public DataSource hsqldbSimpleDriverDataSource (String dbname, String username, String password) throws Exception {
		Driver h2Driver = (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
		return new SimpleDriverDataSource(h2Driver, String.format(HSQLDB_JDBC_URL,dbname),username,password);
	}
}
