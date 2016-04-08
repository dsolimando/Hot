package be.solidx.hot.data.jdbc.js;

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

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet;

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
