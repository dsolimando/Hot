package be.solidx.hot.data.jdbc.groovy;

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

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.AbstractCursor;
import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class Cursor extends AbstractCursor<Map<String, Object>> {

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			AbstractJoinTree<Map<String, Object>> joinTree) {
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			Map<String, Object> whereClauses, 
			AbstractJoinTree<Map<String, Object>> joinTree) {
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata, whereClauses, joinTree);
	}

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			Map<String, Object> whereClauses) {
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata, whereClauses);
	}

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata) {
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata);
	}

	@Override
	protected Map<String, Object> buildMap() {
		return new LinkedHashMap<String, Object>();
	}
	
	@Override
	protected Map<String, Object> put(Map<String, Object> t, String key, Object value) {
		t.put(key, value);
		return t;
	}
}
