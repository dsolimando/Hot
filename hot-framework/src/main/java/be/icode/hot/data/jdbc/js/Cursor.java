package be.icode.hot.data.jdbc.js;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractCursor;
import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

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
