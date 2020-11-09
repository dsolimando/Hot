package be.solidx.hot.data.jdbc.groovy;

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

import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

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
