package be.icode.hot.data.jdbc.groovy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.icode.hot.data.jdbc.AbstractJoinedEntity;
import be.icode.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinedEntity extends AbstractJoinedEntity<Map<String, Object>> {

	public JoinedEntity(
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames, 
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys,
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<Map<String, Object>> parent) {
		super(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}

	@Override
	protected Map<String, Object> buildMap() {
		return new LinkedHashMap<String, Object>();
	}
	
	@Override
	protected Map<String, Object> put(Map<String, Object> entity, String key, Object value) {
		entity.put(key, value);
		return entity;
	}
	
	@Override
	protected List<Map<String, Object>> buildList() {
		return new ArrayList<Map<String, Object>>();
	}

	@Override
	protected Map<String, Object> add(List<Map<String, Object>> list, Map<String, Object> entity, int index) {
		list.add(index, entity);
		return entity;
	}
}
