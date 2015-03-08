package be.solidx.hot.data.jdbc.sql.oracle;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.impl.AbstractDeleteQuery;
import be.solidx.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class OracleDeleteQuery extends AbstractDeleteQuery {

	public OracleDeleteQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
