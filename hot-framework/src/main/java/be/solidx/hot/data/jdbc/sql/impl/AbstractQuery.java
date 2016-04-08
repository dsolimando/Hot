package be.solidx.hot.data.jdbc.sql.impl;

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

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.Query;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

abstract public class AbstractQuery implements Query {

	protected TableMetadata tableMetadata;
	
	protected Criterion rootCriterionGroup;
	
	protected Multimap<String, Object> criterionValues = LinkedListMultimap.create();
	
	public AbstractQuery(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	protected String buildFromClause () {
		return "FROM "+ tablenameWithSchema();
	}
	
	protected String withSchema (String tablename) {
		if (tableMetadata.getSchema() != null) {
			return tableMetadata.getSchema() + "." + tablename;
		}
		return tablename;
	}
	
	protected String tablenameWithSchema () {
		if (tableMetadata.getSchema() != null) {
			return tableMetadata.getSchema() + "." + tableMetadata.getName();
		}
		return tableMetadata.getName();
	}
}
