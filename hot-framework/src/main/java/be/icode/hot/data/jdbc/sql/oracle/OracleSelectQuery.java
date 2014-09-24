package be.icode.hot.data.jdbc.sql.oracle;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.impl.AbstractSelectQuery;

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
