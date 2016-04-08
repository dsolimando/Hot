package be.solidx.hot.data.jdbc;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

@SuppressWarnings("rawtypes")
public abstract class AbstractJoinTree<T extends Map> {

	private Map<String, TableMetadata> tableMetadataMap;
	
	private AbstractJoinedEntity<T> root;
	
	/**
	 * sorted join paths
	 * <br>
	 * a
	 * a.b
	 * a.b.c
	 * a.z
	 * a.z.y
	 * b
	 * b.n
	 * ...
	 * 
	 */
	private List<String> joinPaths;
	
	public AbstractJoinTree(String tablename, List<String> joinPaths, Map<String, TableMetadata> tableMetadataMap) {
		this.tableMetadataMap = tableMetadataMap;
		this.joinPaths = joinPaths;
		
		TableMetadata tableMetadata = tableMetadataMap.get(tablename);
		
		this.root = buildJoinedEntity (
				tablename, 
				0,
				tableMetadata.getColumns().size(),
				tableMetadata.getColumns(), 
				tableMetadata.getPrimaryKeys(),
				null,
				null,
				null);
		
		int startIndex = tableMetadata.getColumns().size();
		for (String joinPath : this.joinPaths) {
			String[] pathTokens = joinPath.split("\\.");
			startIndex = buildDepthFirst(this.root, pathTokens,startIndex);
		}
	}

	private int buildDepthFirst (AbstractJoinedEntity<T> joinedEntity, String[] pathTokens, int startIndex) {
		if (pathTokens.length > 1) {
			int newStartIndex = startIndex;
			for (AbstractJoinedEntity<T> childJE : joinedEntity.getChildren()) {
				int indexGap = childJE.getColumnNames().size();
				if (pathTokens[0].equals(childJE.getTableName())) {
					indexGap = buildDepthFirst(childJE, Arrays.copyOfRange(pathTokens, 1, pathTokens.length), newStartIndex);
				}
				newStartIndex = indexGap;
			}
			return newStartIndex;
		} else {
			TableMetadata tableMetadata = tableMetadataMap.get(pathTokens[0]);
			AbstractJoinedEntity<T> newJoinedEntity = buildJoinedEntity (
					pathTokens[0], 
					startIndex,
					startIndex + tableMetadata.getColumns().size(),
					tableMetadata.getColumns(), 
					tableMetadata.getPrimaryKeys(), 
					tableMetadata.getExportedForeignKeySet(joinedEntity.getTableName()),
					tableMetadata.getForeignKeySet(joinedEntity.getTableName()),
					joinedEntity);
			if (!joinedEntity.getChildren().contains(newJoinedEntity)) {
				joinedEntity.getChildren().add(newJoinedEntity);
			}
			return startIndex + tableMetadata.getColumns().size();
		}
	}
	
	public Map<String, AbstractJoinedEntity<T>> asMap () {
		Map<String, AbstractJoinedEntity<T>> map = new LinkedHashMap<String, AbstractJoinedEntity<T>>();
		map.put(root.getTableName(), root);
		depthFirstTraverse(root, map, root.getTableName());
		return map;
	}
	
	private void depthFirstTraverse (AbstractJoinedEntity<T> parent, Map<String, AbstractJoinedEntity<T>> map, String path) {
		for (AbstractJoinedEntity<T> joinEntity : parent.getChildren()) {
			String childPath = path + "." + joinEntity.getTableName();
			map.put(childPath, joinEntity);
			depthFirstTraverse(joinEntity, map, childPath);
		}
	}
	
	public void addRow (Object[] row) {
		root.addRowData(row);
		depthFirstTraverse(root, row);
	}
	
	private void depthFirstTraverse (AbstractJoinedEntity<T> parent, Object[] row) {
		for (AbstractJoinedEntity<T> joinedEntity : parent.getChildren()) {
			joinedEntity.addRowData(row);
			depthFirstTraverse(joinedEntity, row);
		}
	}
	
	public AbstractJoinedEntity<T> getRoot() {
		return root;
	}
	
	public Map<String, TableMetadata> getTableMetadataMap() {
		return new HashMap<String, TableMetadata>(tableMetadataMap);
	}
	
	protected abstract AbstractJoinedEntity<T> buildJoinedEntity(
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames, 
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys,
			ForeignKeySet foreignKeys,
			AbstractJoinedEntity<T> parent);
}
