package be.icode.hot.data.jdbc.js;

import java.util.List;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.icode.hot.data.jdbc.AbstractJoinedEntity;
import be.icode.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinedEntity extends AbstractJoinedEntity<NativeObject> {

	public JoinedEntity(String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames, 
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys,
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<NativeObject> parent) {
		super(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}

	@Override
	protected NativeObject buildMap() {
		return new NativeObject();
	}

	@Override
	protected NativeObject put(NativeObject entity, String key, Object object) {
		entity.put(key, entity, object);
		return entity;
	}
	
	@Override
	protected NativeArray buildList() {
		return new NativeArray(0);
	}
	
	@Override
	protected NativeObject add(List<NativeObject> list, NativeObject entity, int index) {
		NativeArray nativeArray = (NativeArray) list;
		nativeArray.put(index, nativeArray, entity);
		return entity;
	}
}
