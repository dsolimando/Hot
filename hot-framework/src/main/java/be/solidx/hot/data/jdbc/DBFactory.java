package be.solidx.hot.data.jdbc;

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
