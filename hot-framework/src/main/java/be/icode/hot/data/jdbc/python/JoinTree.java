package be.icode.hot.data.jdbc.python;

import java.util.List;
import java.util.Map;

import org.python.core.PyDictionary;

import be.icode.hot.data.jdbc.AbstractJoinTree;
import be.icode.hot.data.jdbc.AbstractJoinedEntity;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.TableMetadata.ForeignKeySet;

public class JoinTree extends AbstractJoinTree<PyDictionary> {

	public JoinTree(String tablename, List<String> joinPaths, Map<String, TableMetadata> tableMetadataMap) {
		super(tablename, joinPaths, tableMetadataMap);
	}

	@Override
	protected AbstractJoinedEntity<PyDictionary> buildJoinedEntity(
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames,
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys, 
			ForeignKeySet foreignKeys, 
			AbstractJoinedEntity<PyDictionary> parent) {
		return new JoinedEntity(tableName, startIndex, endIndex, columnNames, primaryKeys, exportedForeignKeys, foreignKeys, parent);
	}
}
