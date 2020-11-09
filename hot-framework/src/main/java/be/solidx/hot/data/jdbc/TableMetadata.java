package be.solidx.hot.data.jdbc;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.solidx.hot.data.CollectionMetadata;

public class TableMetadata implements CollectionMetadata {

	private String name;
	
	private String schema;
	
	private List<String> columns = new ArrayList<String>();
	
	private List<String> primaryKeys = new ArrayList<String>();
	
	private Map<String, ForeignKeySet> foreignKeys = new LinkedHashMap<String, TableMetadata.ForeignKeySet>();
	
	private Map<String, ForeignKeySet> exportedForeignKeys = new LinkedHashMap<String, TableMetadata.ForeignKeySet>();

	public TableMetadata(String name, 
			String schema,
			List<String> columns,
			List<String> primaryKeys,
			Map<String, ForeignKeySet> foreignKeys,
			Map<String, ForeignKeySet> exportedForeignKeys) {
		this.name = name;
		this.schema = schema;
		this.columns.addAll(columns);
		if (primaryKeys != null) this.primaryKeys.addAll(primaryKeys);
		if (foreignKeys != null) this.foreignKeys.putAll(foreignKeys);
		if (exportedForeignKeys!= null) this.exportedForeignKeys.putAll(exportedForeignKeys);
	}

	public String getName() {
		return name;
	}

	public String getSchema() {
		return schema;
	}

	@Override
	public List<String> getColumns() {
		return columns;
	}
	
	@Override
	public List<String> getPrimaryKeys() {
		return primaryKeys;
	}
	
	public ForeignKeySet getForeignKeySet (String tablename) {
		return foreignKeys.get(tablename);
	}
	
	public ForeignKeySet getExportedForeignKeySet (String tablename) {
		return exportedForeignKeys.get(tablename);
	}
	
	public List<String> getRelations () {
		ArrayList<String> relations = new ArrayList<String>();
		relations.addAll(exportedForeignKeys.keySet());
		relations.addAll(foreignKeys.keySet());
		return relations;
	}
	
	public static class ForeignKeySet {

		private String table;
		
		private String schema;
		
		private List<String> targetPKNameList;
		
		private List<String> foreignKeyNameList;

		public ForeignKeySet(String schema, String table, List<String> targetPKNameList, List<String> foreignKeyNameList) {
			this.schema = schema;
			this.table = table;
			this.targetPKNameList = new ArrayList<String>(targetPKNameList);
			this.foreignKeyNameList = new ArrayList<String>(foreignKeyNameList);
		}
		
		public String getTable() {
			return table;
		}
		
		public String getSchema() {
			return schema;
		}
		
		public List<String> getForeignKeyNameList() {
			return new ArrayList<String>(foreignKeyNameList);
		}
		
		public List<String> getTargetPKNameList() {
			return new ArrayList<String>(targetPKNameList);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof ForeignKeySet) {
				ForeignKeySet foreignKeySet = (ForeignKeySet) obj;
				return foreignKeySet.getSchema().equals(this.getSchema()) && foreignKeySet.getTable().equals(this.getTable());
			}
			return false;
		}
	}
}
