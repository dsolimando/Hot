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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class JoinableCollection extends Collection implements be.solidx.hot.data.jdbc.JoinableCollection<Map<String, Object>> {
	
	protected Map<String, TableMetadata> tableMetadataMap;
	
	public JoinableCollection(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			String name, 
			Map<String, TableMetadata> tableMetadataMap) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadataMap.get(name));
		this.tableMetadataMap = tableMetadataMap;
	}

	public Collection join (List<String> joinPaths) {
		JoinTree resultSetEntityTree = new JoinTree(tableMetadata.getName(), joinPaths, tableMetadataMap);
		Collection basicCollection = new Collection(namedParameterJdbcTemplate, queryBuilder, tableMetadata, resultSetEntityTree);
		return basicCollection;
	}

	@Override
	protected Map<String, Object> buildMap() {
		return new LinkedHashMap<String, Object>();
	}
}
