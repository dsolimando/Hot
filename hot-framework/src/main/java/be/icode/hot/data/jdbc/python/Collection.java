package be.icode.hot.data.jdbc.python;

import java.util.Map;

import org.python.core.PyDictionary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractCollection;
import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

public class Collection extends AbstractCollection<PyDictionary> {

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			TableMetadata tableMetadata, 
			AbstractJoinTree<PyDictionary> joinsTree) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata, joinsTree);
	}

	public Collection(NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			TableMetadata tableMetadata) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadata);
	}

	@Override
	protected PyDictionary buildMap() {
		return new PyDictionary();
	}
	
	@Override
	protected Cursor buildCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate,
			TableMetadata tableMetadata, 
			Map<String, Object> criteria, 
			AbstractJoinTree<PyDictionary> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata,criteria, joinTree);
	}

	@Override
	protected Cursor buildCursor(QueryBuilder queryBuilder, 
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			TableMetadata tableMetadata,
			AbstractJoinTree<PyDictionary> joinTree) {
		return new Cursor(queryBuilder, namedParameterJdbcTemplate, tableMetadata,joinTree);
	}
}
