package be.icode.hot.spring.config;

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

import be.icode.hot.data.DB;
import be.icode.hot.data.jdbc.DBFactory;
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

@Configuration
@Import({CommonConfig.class, GaeConfig.class, MongoConfig.class, ThreadPoolsConfig.class})
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
	GaeConfig gaeConfig;
	
	@Autowired
	MongoConfig mongoConfig;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	ThreadPoolsConfig threadPoolsConfig;
	
	@Bean
	public Map<String,DB<Map<String, Object>>> groovyDbMap() throws Exception {
		Map<String,DB<Map<String, Object>>> dbMap = new LinkedHashMap<String, DB<Map<String, Object>>>();
		if (hotConfig.getNature().equals(HotConfig.GAE)) {
			dbMap.put(HotConfig.GAE, gaeConfig.groovyGaeDB());
		} else {
			for (DBFactory dbFactory: dbFactories()) {
				dbMap.put(dbFactory.getName(),dbFactory.buildGroovyDB());
			}
			dbMap.putAll(mongoConfig.groovyMongoDb());
		}
		return dbMap;
	}
	
	@Bean
	public Map<String, DB<NativeObject>> jsDbMap() throws Exception {
		Map<String, DB<NativeObject>> dbMap = new LinkedHashMap<String, DB<NativeObject>>();
		if (hotConfig.getNature().equals(HotConfig.GAE)) {
			dbMap.put(HotConfig.GAE, gaeConfig.jsGaeDB());
		} else {
			for (DBFactory dbFactory: dbFactories()) {
				dbMap.put(dbFactory.getName(),dbFactory.buildJsDB());
			}
			dbMap.putAll(mongoConfig.jsMongoDB());
		}
		return dbMap;
	}
	
	@Bean
	public Map<String, DB<PyDictionary>> pythonDbMap() throws Exception {
		Map<String, DB<PyDictionary>> dbMap = new LinkedHashMap<String, DB<PyDictionary>>();
		if (hotConfig.getNature().equals(HotConfig.GAE)) {
			dbMap.put(HotConfig.GAE, gaeConfig.pyGaeDB());
		} else {
			for (DBFactory dbFactory: dbFactories()) {
				dbMap.put(dbFactory.getName(),dbFactory.buildPyDB());
			}
			dbMap.putAll(mongoConfig.pyMongoDB());
		}
		return dbMap;
	}
	
	@Bean
	public List<DBFactory> dbFactories () throws Exception {
		List<DBFactory> dbFactories = new ArrayList<DBFactory>();
		for (Entry<be.icode.hot.spring.config.HotConfig.DataSource, DataSource> entry : dataSources().entrySet()) {
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
	public Map<be.icode.hot.spring.config.HotConfig.DataSource, DataSource> dataSources () throws Exception {
		Map<be.icode.hot.spring.config.HotConfig.DataSource, DataSource> dataSources = new HashMap<be.icode.hot.spring.config.HotConfig.DataSource, DataSource>();
		for (be.icode.hot.spring.config.HotConfig.DataSource dataSource : hotConfig.getDataSources()) {
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
