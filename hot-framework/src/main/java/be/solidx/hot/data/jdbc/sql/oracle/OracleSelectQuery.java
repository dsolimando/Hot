package be.solidx.hot.data.jdbc.sql.oracle;

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

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.impl.AbstractSelectQuery;

public class OracleSelectQuery extends AbstractSelectQuery {
	
	public OracleSelectQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}
	
	@Override
	public String build() {
		String query = null;
		if (joinTree == null) {
			query = selectOrCount() + buildFromClause();
		} else {
			query = selectOrCount() + buildLeftOuterJoin();
		}
		if (rootCriterionGroup != null) {
			query += buildWhereClauses();
		}
		if (count) {
			return query.trim();
		}
		if (!sortMap.keySet().isEmpty()) {
			query += buildOrderBy();
		}
		if (limit != null || skip != null) {
			if (skip != null) {
				String limitWrappedQuery = String.format("SELECT a.*, ROWNUM rn FROM ( %s ) a ", query);
				if (limit != null) {
					limitWrappedQuery += String.format("WHERE ROWNUM <= %s ",limit+skip);
				}
				return String.format("SELECT * FROM ( %s ) WHERE rn > %s" , limitWrappedQuery, skip).trim();
			} 
			return String.format("SELECT * FROM ( %s ) WHERE ROWNUM <= %s ", query, limit).trim();
		}
		return query.trim();
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
