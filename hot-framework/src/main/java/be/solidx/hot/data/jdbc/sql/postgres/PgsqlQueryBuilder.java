package be.solidx.hot.data.jdbc.sql.postgres;

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

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.Query;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.data.jdbc.sql.impl.InsertQuery;

public class PgsqlQueryBuilder implements QueryBuilder {
	
	private static PgsqlQueryBuilder pgsqlQueryBuilder;
	
	private CriterionFactory criterionFactory = new PgsqlCriterionFactory();
	
	private PgsqlQueryBuilder() {}
	
	public static PgsqlQueryBuilder getInstance() {
		if (pgsqlQueryBuilder == null) {
			pgsqlQueryBuilder = new PgsqlQueryBuilder();
		}
		return pgsqlQueryBuilder;
	}

	@Override
	public SelectQuery buildSelectQuery(TableMetadata tableMetadata) {
		return new PgsqlSelectQuery(tableMetadata, criterionFactory);
	}

	@Override
	public QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata) {
		return new PgsqlDeleteQuery(tableMetadata, criterionFactory);
	}

	@Override
	public Query buildInsertQuery(TableMetadata tableMetadata, List<String> insertParamaters) {
		return new InsertQuery(tableMetadata, insertParamaters);
	}

	@Override
	public QueryWithCriteria buildUpdateQuery(TableMetadata tableMetadata, Map<String,Object> updateParameters) {
		return new PgsqlUpdateQuery(tableMetadata, updateParameters, criterionFactory);
	}

}
