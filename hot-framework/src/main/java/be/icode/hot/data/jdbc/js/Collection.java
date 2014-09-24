package be.icode.hot.data.jdbc.js;

import java.util.Map;

import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractCollection;
import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

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
