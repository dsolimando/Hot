package be.solidx.hot.data.jdbc.python;

import java.util.List;

import org.python.core.PyDictionary;
import org.python.core.PyList;

import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinedEntity extends AbstractJoinedEntity<PyDictionary> {

	public JoinedEntity(String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames, 
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys,
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<PyDictionary> parent) {
		super(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}

	@Override
	protected PyDictionary put(PyDictionary entity, String key, Object value) {
		entity.put(key, value);
		return entity;
	}

	@Override
	protected PyDictionary buildMap() {
		return new PyDictionary();
	}
	
	@Override
	protected PyList buildList() {
		return new PyList();
	}
	
	@Override
	protected PyDictionary add(List<PyDictionary> list, PyDictionary entity, int index) {
		list.add(index, entity);
		return entity;
	}
}
