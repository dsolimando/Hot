package be.icode.hot.data.jdbc.groovy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractCollection;
import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

public class Collection extends AbstractCollection<Map<String, Object>> {

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			TableMetadata tableMetadata,
			JoinTree joinsTree) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata, joinsTree);
	}

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, QueryBuilder queryBuilder, TableMetadata tableMetadata) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata);
	}

	@Override
	protected Map<String, Object> buildMap() {
		return new LinkedHashMap<String, Object>();
	}

	@Override
	protected Cursor buildCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			Map<String, Object> whereClauses, 
			AbstractJoinTree<Map<String, Object>> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, whereClauses, joinTree);
	}

	@Override
	protected Cursor buildCursor(QueryBuilder queryBuilder, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TableMetadata tableMetadata,
			AbstractJoinTree<Map<String, Object>> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata, joinTree);
	}
}
