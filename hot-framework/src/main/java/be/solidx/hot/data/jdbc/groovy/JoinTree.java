package be.solidx.hot.data.jdbc.groovy;

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
