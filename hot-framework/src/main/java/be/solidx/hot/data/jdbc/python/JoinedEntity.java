package be.solidx.hot.data.jdbc.python;

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
