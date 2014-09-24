package be.icode.hot.data.jdbc.groovy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractCursor;
import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

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
