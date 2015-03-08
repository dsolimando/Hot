package be.solidx.hot.data.jdbc.sql.impl;

import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;


abstract public class AbstractDeleteQuery extends AbstractQueryWithCriteria {

	public AbstractDeleteQuery(TableMetadata tableMetadata, Map<String, Object> whereClauses, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
		addWhereClauses(whereClauses);
	}

	public AbstractDeleteQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}

	public String build() {
		String queryString = "DELETE " + buildFromClause();
		if (!criterionValues.isEmpty())
			queryString += " " + buildWhereClauses().trim();
		System.out.println(queryString);
		return queryString;
	}
}
