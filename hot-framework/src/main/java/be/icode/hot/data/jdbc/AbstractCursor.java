package be.icode.hot.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.Cursor;
import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.SelectQuery;
import be.icode.hot.utils.CollectionUtils;

@SuppressWarnings("rawtypes")
public abstract class AbstractCursor<T extends Map> implements Cursor<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCursor.class);

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private Map<String, Object> criterionValues;

	private SelectQuery query;
	
	private AbstractJoinTree<T> joinTree;
	
	public AbstractCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata, 
			Map<String, Object> whereClauses, 
			AbstractJoinTree<T> joinTree) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.joinTree = joinTree;
		query = queryBuilder.buildSelectQuery(tableMetadata);
		this.criterionValues = CollectionUtils.flat(query.addWhereClauses(whereClauses));
		query.addJoins(joinTree);
	}

	public AbstractCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			AbstractJoinTree<T> joinTree) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.joinTree = joinTree;
		query = queryBuilder.buildSelectQuery(tableMetadata);
		query.addJoins(joinTree);
	}
	
	public AbstractCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata, 
			Map<String, Object> whereClauses) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		query = queryBuilder.buildSelectQuery(tableMetadata);
		criterionValues = CollectionUtils.flat(query.addWhereClauses(whereClauses));
	}
	
	public AbstractCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		query = queryBuilder.buildSelectQuery(tableMetadata);
	}
	
	@Override
	public Iterator<T> iterator() {
		String queryString = query.build();
		
		if (LOGGER.isDebugEnabled()) LOGGER.debug(queryString);
		
		if (joinTree != null) {
			List<Object[]> results = namedParameterJdbcTemplate.query(queryString, criterionValues,new ResultSetExtractor<List<Object[]>>() {
				@Override
				public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
					int numcol = rs.getMetaData().getColumnCount();
					List<Object[]> results = new ArrayList<Object[]>();
					while (rs.next()) {
						int start = 1;
						// DB2 Paging case
						if (rs.getMetaData().getColumnLabel(1).equals("RN")) {
							start = 2;
						}
						Object[] result = new Object[numcol];
						for (int i=start; i <= numcol; ++i) {
							result[i-start] = rs.getObject(i);
						}
						results.add(result);
					}
					return results;
				}
			});
			return reduce(results).iterator();
		} else {
			List<T> results = namedParameterJdbcTemplate.query(queryString, criterionValues, new ResultSetExtractor<List<T>>() {
				@Override
				public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					int numcol = rs.getMetaData().getColumnCount();
					List<T> results = new ArrayList<T>();
					while (rs.next()) {
						T result = buildMap();
						for (int i=1; i <= numcol; ++i) {
							put(result,rs.getMetaData().getColumnLabel(i).toLowerCase(),rs.getObject(i));
							//result.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
						}
						results.add(result);
					}
					return results;
				}
			});
			return results.iterator();
		}
	}
	
	protected abstract T buildMap();
	
	protected abstract T put (T t, String key, Object value);
	
	@Override
	public Integer count() {
		return namedParameterJdbcTemplate.queryForInt(query.count().build(), criterionValues);
	}

	@Override
	public Cursor<T> limit(Integer limit) {
		query.limit(limit);
		return this;
	}

	@Override
	public Cursor<T> skip(Integer at) {
		query.skip(at);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cursor<T> sort(T sortMap) {
		query.getSortMap().putAll(sortMap);
		return this;
	}
	
	@Override
	public String toString() {
		
		String asString = "";
		for (Map<String, Object> item : this) {
			asString += item + "\n";
		}
		return asString;
	}
	
	private Collection<T> reduce(List<Object[]> results) {
		for (Object[] row : results) {
			joinTree.addRow(row);
		}
		return joinTree.getRoot().getCache().values();
	}
}
