package be.solidx.hot.data.jdbc;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;

import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

public class DBFactory {

	private String name;
	
	private DBEngine engine;
	
	private String schema;
	
	private DataSource dataSource;
	
	private QueryBuilder queryBuilder;
	
	@PostConstruct
	public void init () {
		queryBuilder = QueryBuilderFactory.buildQueryBuilder(engine);
	}

	public DB<Map<String, Object>> buildGroovyDB() {
		return new be.solidx.hot.data.jdbc.groovy.DB(queryBuilder, dataSource, schema);
	}
	
	public DB<NativeObject> buildJsDB() {
		return new be.solidx.hot.data.jdbc.js.DB(queryBuilder, dataSource, schema);
	}
	
	public DB<PyDictionary> buildPyDB() {
		return new be.solidx.hot.data.jdbc.python.DB(queryBuilder, dataSource, schema);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setEngine(DBEngine engine) {
		this.engine = engine;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
}
