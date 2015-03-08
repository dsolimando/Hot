package be.solidx.hot.data.jdbc.groovy;

import java.util.List;
import java.util.Map;

import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinTree extends AbstractJoinTree<Map<String, Object>> {

	public JoinTree(String tablename, List<String> joinPaths, Map<String, TableMetadata> tableMetadataMap) {
		super(tablename, joinPaths, tableMetadataMap);
	}

	@Override
	protected AbstractJoinedEntity<Map<String, Object>> buildJoinedEntity(
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> 
			columnNames,
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys, 
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<Map<String, Object>> parent) {
		return new JoinedEntity(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}

}
