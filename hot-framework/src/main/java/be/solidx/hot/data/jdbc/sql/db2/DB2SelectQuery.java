package be.solidx.hot.data.jdbc.sql.db2;

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

import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.impl.AbstractSelectQuery;
import be.solidx.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class DB2SelectQuery extends AbstractSelectQuery {
	
	protected final String ROWNUM = "rn";
	
	public DB2SelectQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected String selectOrCount() {
		if (count) {
			return "SELECT COUNT(*) ";
		} else {
			if (skip != null) {
				String pKeys = "";
				String separator = "";
				for (String pk : tableMetadata.getPrimaryKeys()) {
					pKeys += separator + tablenameWithSchema() + "." + pk + " ASC";
					separator = ",";
				}
				separator = "";
				String tables = "";
				if (joinTree != null) {
					for (AbstractJoinedEntity<? extends Map> joinedEntity : joinTree.asMap().values()) {
						for (String  column : joinedEntity.getColumnNames()) {
							tables += String.format("%s %s.%s AS %s_%s", 
									separator,
									withSchema(joinedEntity.getTableName()),
									column,
									joinedEntity.getTableName(),
									column);
							separator = ",";
						}
					}
				} else {
					tables = String.format(" %s.*", tablenameWithSchema()); 
				}
				return "SELECT ROW_NUMBER() OVER (ORDER BY "+ pKeys  +") AS "+ROWNUM+","+tables+" ";
			} else {
				return "SELECT * ";
			}
		}
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
				String finalQuery = String.format("SELECT * FROM (%s) WHERE", query);
				finalQuery += String.format(" %s > %s", ROWNUM, skip);
				if (limit != null) {
					finalQuery += String.format(" AND %s <= %s ", ROWNUM, limit+skip);
				}
				return finalQuery;
			} else {
				return query + String.format(" FETCH FIRST %d ROWS ONLY", limit);
			}
		}
		return query.trim();
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
	
	@Override
	protected CriterionImpl buildModuloCriterion(String parameterName, int index) {
		return new ModuloCriterion(parameterName, index);
	};
}
