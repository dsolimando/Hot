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

import java.util.HashMap;
import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.AbstractJoinTree;
import be.solidx.hot.data.jdbc.AbstractJoinedEntity;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.SelectQuery;

abstract public class AbstractSelectQuery extends AbstractQueryWithCriteria implements SelectQuery {

	protected Integer limit; 
	
	protected Integer skip; 
	
	protected Map<String, Object> sortMap = new HashMap<String, Object>();
	
	protected boolean count;
	
	@SuppressWarnings("rawtypes")
	protected AbstractJoinTree<? extends Map> joinTree;
	
	public AbstractSelectQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}
	
	protected String selectOrCount () {
		return count?"SELECT COUNT(*) ":"SELECT * ";
	}

	@SuppressWarnings("rawtypes")
	protected String buildLeftOuterJoin () {
		String loj = "";
		for (AbstractJoinedEntity<? extends Map> joinedEntity : joinTree.asMap().values()) {
			if (joinedEntity.getParent() != null) {
				loj += String.format("LEFT OUTER JOIN %s ", withSchema(joinedEntity.getTableName()));
				String separator = "ON";
				if (joinedEntity.getForeignKeys() != null) {
					// One To Many
					for (int i = 0; i < joinedEntity.getForeignKeys().getForeignKeyNameList().size(); i++) {
						loj += String.format("%s %s.%s = %s.%s ", 
								separator,
								withSchema(joinedEntity.getTableName()),
								joinedEntity.getForeignKeys().getForeignKeyNameList().get(i),
								withSchema(joinedEntity.getParent().getTableName()),
								joinedEntity.getForeignKeys().getTargetPKNameList().get(i));
						separator = "AND";
					}
				} else {
					for (int i = 0; i < joinedEntity.getExportedForeignKeys().getForeignKeyNameList().size(); i++) {
						loj += String.format("%s %s.%s = %s.%s ", 
								separator,
								withSchema(joinedEntity.getTableName()),
								joinedEntity.getExportedForeignKeys().getTargetPKNameList().get(i),
								withSchema(joinedEntity.getParent().getTableName()),
								joinedEntity.getExportedForeignKeys().getForeignKeyNameList().get(i));
						separator = "AND";
					}
				}
				
			} else {
				loj += String.format("FROM %s ", withSchema(joinedEntity.getTableName()));
			}
		}
		return loj.trim();
	}
	
	protected String buildOrderBy () {
		String query = "";
		String separator = "";
		for (String key : sortMap.keySet()) {
			Object value = sortMap.get(key);
			if (value instanceof Integer) {
				Integer intValue = (Integer) value;
				String ascOrDesc = intValue == -1? " DESC":" ASC";
				String realKey = key;
				if (joinTree != null && key.split("\\.").length < 2) {
					realKey = tablenameWithSchema() + "." + key;
				}
				query += separator + realKey + ascOrDesc;
				separator = ", ";
			}
		}
		return query.isEmpty()?query:" ORDER BY "+query;
	}
	
	@Override
	public Map<String, Object> getSortMap() {
		return sortMap;
	}
	
	@Override
	public SelectQuery limit(Integer limit) {
		this.limit = limit;
		return this;
	}
	
	@Override
	public SelectQuery skip(Integer skip) {
		this.skip = skip;
		return this;
	}
	
	@Override
	public SelectQuery count() {
		this.count = true;
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public SelectQuery addJoins(AbstractJoinTree<? extends Map> joinTree) {
		this.joinTree = joinTree;
		return this;
	}
}
