package be.solidx.hot.data.jdbc.python;

import java.util.Map;

import org.python.core.PyDictionary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.AbstractCursor;
import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class Cursor extends AbstractCursor<PyDictionary>{

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			AbstractJoinTree<PyDictionary> joinTree) {
		super(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}

	public Cursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			Map<String, Object> whereClauses, 
			AbstractJoinTree<PyDictionary> joinTree) {
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
	protected PyDictionary buildMap() {
		return new PyDictionary();
	}

	@Override
	protected PyDictionary put(PyDictionary t, String key, Object value) {
		t.put(key, value);
		return t;
	}

}
