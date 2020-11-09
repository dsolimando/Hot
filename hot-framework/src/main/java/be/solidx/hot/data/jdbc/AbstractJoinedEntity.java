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

import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

@SuppressWarnings("rawtypes")
abstract public class AbstractJoinedEntity<T extends Map> {

	private String tableName;
	private List<String> columnNames = new ArrayList<String>();
	private List<String> primaryKeys;
	/*
	 * Foreign Keys exported by parent entity targeting child primary keys. Mutually exclusive with foreignKeys
	 */
	private ForeignKeySet exportedForeignKeys;
	/*
	 * Foreign Keys pointing to parent primary keys. Mutually exclusive with exportedForeignKeys
	 */
	private ForeignKeySet foreignKeys;
	
	private int resultsetStartIndex;
	private int resultsetEndIndex;
	
	private AbstractJoinedEntity<T> parent;
	private List<AbstractJoinedEntity<T>> children = new ArrayList<AbstractJoinedEntity<T>>();
	
	/**
	 * cache containing all row data retrieved from the result set corresponding to this {@link AbstractJoinedEntity}
	 */
	private Map<Long, T> cache = new LinkedHashMap<Long, T>();
	
	public AbstractJoinedEntity (
			String tableName, 
			int startIndex, 
			int endIndex, 
			List<String> columnNames, 
			List<String> primaryKeys, 
			ForeignKeySet exportedForeignKeys,
			ForeignKeySet foreignKeys,
			AbstractJoinedEntity<T> parent) {
		this.tableName = tableName;
		this.resultsetStartIndex = startIndex;
		this.resultsetEndIndex = endIndex;
		this.columnNames = columnNames;
		this.primaryKeys = primaryKeys;
		this.foreignKeys = foreignKeys;
		this.exportedForeignKeys = exportedForeignKeys;
		this.parent = parent;
		if (parent != null) {
			this.parent.getChildren().add(this);
		}
	}

	public long computeHash(List<Object> values) {
		long hash = 0l;
		for (Object object : values) {
			if (object == null) {
				hash = -1l;
				break;
			}
			hash += object.hashCode();
		}
		return hash;
	}

	@SuppressWarnings("unchecked")
	public void addRowData (Object[] rowValues) {
		// We create the entity
		T entity = buildMap();
		for (int i = 0, j=resultsetStartIndex; j < resultsetEndIndex; i++,j++) {
			put (entity, columnNames.get(i), rowValues[j]);
			//entity.put(columnNames.get(i), rowValues[j]);
		}
		// We retrieve pk values
		List<Object> pkValues = new ArrayList<Object>();
		for (String primaryKey : primaryKeys) {
			pkValues.add(entity.get(primaryKey));
		}
		long pkHash = computeHash(pkValues);
		// We add the entity to the cache
		if (cache.get(pkHash) == null) {
			cache.put(pkHash, entity);
		} else {
			// Entity already exists in the cache, we reuse it
			entity = cache.get(pkHash);
		}
		// Root entity case
		if (this.getParent() == null) {
			return;
		}
		if (foreignKeys != null) {
			// One to Many case
			List<Object> fkValues = new ArrayList<Object>();
			for (String fk : foreignKeys.getForeignKeyNameList()) {
				fkValues.add(entity.get(fk));
				entity.remove(fk);
			}
			long fkHash = computeHash(fkValues);
			if (fkHash != -1l) {
				// We associate this entity to his parent
				List<T> siblings = (List<T>) parent.getCache().get(fkHash).get(tableName);
				if (siblings == null) {
					//siblings = new ArrayList<Map<String,Object>>();
					siblings = buildList();
					put(parent.getCache().get(fkHash),tableName,siblings);
					//parent.getCache().get(fkHash).put(tableName,siblings);
				}
				if (!siblings.contains(entity)) add(siblings, entity, siblings.size());
			}
		} else {
			// Many to One case
			if (exportedForeignKeys == null) {
				throw new RuntimeException();
			}
			List<Object> exportedPkValues = new ArrayList<Object>();
			for (String pk : exportedForeignKeys.getTargetPKNameList()) {
				exportedPkValues.add(entity.get(pk));
			}
			long exportedPkHash = computeHash(exportedPkValues);
			if (exportedPkHash != -1l) {
				// We associate this entity to his parent
				for (Map<String, Object> parentEntity: parent.getCache().values()) {
					List<Object> exportedFkValues = new ArrayList<Object>();
					for (String exportedFk : exportedForeignKeys.getForeignKeyNameList()) {
						exportedFkValues.add(parentEntity.get(exportedFk));
					}
					long exportedFkHash = computeHash(exportedFkValues);
					if (exportedFkHash == exportedPkHash) {
						String noSTableTame = tableName.endsWith("s")? tableName.substring(0, tableName.length()-1):tableName;
						put((T) parentEntity,noSTableTame,entity);
					}
				}
			}
		}
	}
	
	protected abstract T put(T entity, String key, Object value);

	protected abstract T buildMap ();
	
	protected abstract List<T> buildList();
	
	protected abstract T add(List<T> list, T entity, int index);
	
	public List<String> getPrimaryKeys() {
		return primaryKeys;
	}
	
	public Map<Long, T> getCache() {
		return cache;
	}
	
	public List<AbstractJoinedEntity<T>> getChildren() {
		return children;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public ForeignKeySet getForeignKeys() {
		return foreignKeys;
	}
	
	public ForeignKeySet getExportedForeignKeys() {
		return exportedForeignKeys;
	}
	
	public int getResultsetStartIndex() {
		return resultsetStartIndex;
	}
	
	public int getResultsetEndIndex() {
		return resultsetEndIndex;
	}
	
	public AbstractJoinedEntity<T> getParent() {
		return parent;
	}
}
