package be.solidx.hot.data.jdbc.sql.postgres;

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.jdbc.AbstractSqlCriterionFactory;

public class PgsqlCriterionFactory extends AbstractSqlCriterionFactory {

	@Override
	protected Criterion buildRegexpCriterion(String parameterName, String value, int index) {
		return new RegexpCriterion(parameterName, index);
	}

}
