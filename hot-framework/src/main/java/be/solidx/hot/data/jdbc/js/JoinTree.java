package be.solidx.hot.data.jdbc.js;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeObject;

import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinTree extends AbstractJoinTree<NativeObject> {

	public JoinTree(String tablename, List<String> joinPaths, Map<String, TableMetadata> tableMetadataMap) {
		super(tablename, joinPaths, tableMetadataMap);
	}

	@Override
	protected AbstractJoinedEntity<NativeObject> buildJoinedEntity(
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames,
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys, 
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<NativeObject> parent) {
		return new JoinedEntity(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}

}
