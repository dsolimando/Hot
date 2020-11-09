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

import java.util.Map;

import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.AbstractCollection;
import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class Collection extends AbstractCollection<NativeObject> {

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			TableMetadata tableMetadata, 
			JoinTree joinsTree) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata, joinsTree);
	}

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			TableMetadata tableMetadata) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata);
	}
	
	@Override
	protected NativeObject buildMap() {
		return new NativeObject();
	}

	@Override
	protected Cursor buildCursor(
			QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate,
			TableMetadata tableMetadata, 
			Map<String, Object> whereClauses, 
			AbstractJoinTree<NativeObject> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, whereClauses, joinTree);
	}

	@Override
	protected Cursor buildCursor(
			QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate,
			TableMetadata tableMetadata, 
			AbstractJoinTree<NativeObject> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}
}
