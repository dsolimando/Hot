package be.solidx.hot.data.jdbc.sql.oracle;

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.jdbc.AbstractSqlCriterionFactory;

public class OracleCriterionFactory extends AbstractSqlCriterionFactory {

	@Override
	protected Criterion buildRegexpCriterion(String parameterName, String value, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
