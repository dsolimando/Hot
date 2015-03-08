package be.solidx.hot.data.jdbc.sql.mysql;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.impl.AbstractSelectQuery;

public class MysqlSelectQuery extends AbstractSelectQuery implements SelectQuery {

	public MysqlSelectQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}
	
	public String build () {
		String query = null;
		if (joinTree == null) {
			query = selectOrCount() + buildFromClause();
		} else {
			query = selectOrCount() + buildLeftOuterJoin();
		}
		if (rootCriterionGroup != null) {
			query += buildWhereClauses();
		}
		//query = query.trim();
		
		if (!sortMap.keySet().isEmpty()) {
			query += buildOrderBy();
		}
		if (count) return query;
		
		if (limit != null) {
			query += " LIMIT " + limit;
			if (skip != null) {
				query += " OFFSET " + skip;
			}
		}
		return query.trim();
	}
	
	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
