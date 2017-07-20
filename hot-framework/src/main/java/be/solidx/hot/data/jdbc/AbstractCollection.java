package be.solidx.hot.data.jdbc;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import be.solidx.hot.data.Collection;
import be.solidx.hot.data.Cursor;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.utils.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@SuppressWarnings("rawtypes")
public abstract class AbstractCollection<T extends Map> implements Collection<T> {
	
protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	protected QueryBuilder queryBuilder;
	
	protected AbstractJoinTree<T> joinTree;
	
	protected TableMetadata tableMetadata;
	
	public AbstractCollection(
			final NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			final QueryBuilder queryBuilder, 
			final TableMetadata tableMetadata) {
		
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.queryBuilder = queryBuilder;
		this.tableMetadata = tableMetadata;
	}
	
	public AbstractCollection(
			final NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			final QueryBuilder queryBuilder, 
			final TableMetadata tableMetadata, 
			final AbstractJoinTree<T> joinsTree) {
		
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.queryBuilder = queryBuilder;
		this.joinTree = joinsTree;
		this.tableMetadata = tableMetadata;
	}

	@Override
	public T findOne(final T whereClauses) {
		ArrayList<T> results = Lists.newArrayList(find(whereClauses));
		if (results.size() > 0) {
			return results.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cursor<T> find(final T whereClauses) {
		if (whereClauses != null && whereClauses.size() > 0) {
			return buildCursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, whereClauses, joinTree);
		} else {
			return find();
		}
	}

	@Override
	public Cursor<T> find() {
		return buildCursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long count(final T whereClauses) {
		SelectQuery query = queryBuilder.buildSelectQuery(tableMetadata);
		Map<String, Object> parameterValues = CollectionUtils.flat(query.addWhereClauses(whereClauses));
		query.addJoins(joinTree);
		return namedParameterJdbcTemplate.queryForObject(query.count().build(), parameterValues, Long.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T update(T whereClauses, T values) {
		QueryWithCriteria query = queryBuilder.buildUpdateQuery(tableMetadata, values);
		Multimap<String, Object> parameterValues = query.addWhereClauses(whereClauses);
		namedParameterJdbcTemplate.update(query.build(), CollectionUtils.flat(parameterValues));
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<T> remove(T whereClauses) {
		QueryWithCriteria query = queryBuilder.buildDeleteQuery(tableMetadata);
		Map<String, Object> parameterValues = CollectionUtils.flat(query.addWhereClauses(whereClauses));
		namedParameterJdbcTemplate.update(query.build(), parameterValues);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T insert(T values) {
		T results = buildMap();
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(
				queryBuilder.buildInsertQuery(tableMetadata, new ArrayList<String>(values.keySet())).build(),
				new MapSqlParameterSource(values),
				generatedKeyHolder);
		if(generatedKeyHolder.getKey() != null) results.putAll(generatedKeyHolder.getKeys());
		return results;
	}

	@Override
	public Collection<T> drop() {
		namedParameterJdbcTemplate.update(queryBuilder.buildDeleteQuery(tableMetadata).build(), new HashMap<String, Object>());
		return this;
	}
	
	static class LowercaseKeyMapResultsetExtractor implements ResultSetExtractor<List<Map<String, Object>>> {
		@Override
		public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException, DataAccessException {
			int numcol = rs.getMetaData().getColumnCount();
			List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
			while (rs.next()) {
				HashMap<String, Object> result = new LinkedHashMap<String, Object>();
				int start = 1;
				// DB2 Paging case
				if (rs.getMetaData().getColumnLabel(1).equals("RN")) start = 2;
				for (int i=start; i <= numcol; ++i) {
					result.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
				}
				results.add(result);
			}
			return results;
		}
	}
	
	protected abstract T buildMap();
	
	protected abstract Cursor<T> buildCursor (QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata, 
			Map<String, Object> whereClauses, 
			AbstractJoinTree<T> joinTree);
	
	protected abstract Cursor<T> buildCursor (QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata, 
			AbstractJoinTree<T> joinTree);
}
