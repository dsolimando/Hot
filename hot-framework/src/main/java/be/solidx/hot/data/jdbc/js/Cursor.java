package be.solidx.hot.data.jdbc.js;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.AbstractCursor;
import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class Cursor extends AbstractCursor<NativeObject> {

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata, 
			AbstractJoinTree<NativeObject> joinTree) {
		
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			Map<String, Object> whereClauses, 
			AbstractJoinTree<NativeObject> joinTree) {
		
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
	protected NativeObject buildMap() {
		return new NativeObject();
	}
	
	@Override
	protected NativeObject put(NativeObject map, String key, Object value) {
		map.put(key, map, value);
		return map;
	}
	
	public NativeArray toArray() {
		ArrayList<NativeObject> nos = new ArrayList<NativeObject>();
		Iterator<NativeObject> it = iterator();
		while (it.hasNext()) {
			nos.add(it.next());
		}
		return new NativeArray(nos.toArray());
	}
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
