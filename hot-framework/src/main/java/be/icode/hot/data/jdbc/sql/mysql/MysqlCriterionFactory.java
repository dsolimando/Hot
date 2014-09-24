package be.icode.hot.data.jdbc.sql.mysql;

import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.jdbc.AbstractSqlCriterionFactory;

public class MysqlCriterionFactory extends AbstractSqlCriterionFactory {

	@Override
	protected Criterion buildRegexpCriterion(String parameterName, String value, int index) {
		return new RegexpCriterion(parameterName, index);
	}

}
