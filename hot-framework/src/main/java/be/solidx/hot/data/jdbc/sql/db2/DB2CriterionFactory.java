package be.solidx.hot.data.jdbc.sql.db2;

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.jdbc.AbstractSqlCriterionFactory;

public class DB2CriterionFactory extends AbstractSqlCriterionFactory {

	@Override
	protected Criterion buildModuloCriterion(String parameterName, int moduloValue, int moduloResult, int index) {
		return new ModuloCriterion(parameterName, index);
	}

	@Override
	protected Criterion buildRegexpCriterion(String parameterName, String value, int index) {
		return new RegexpCriterion(parameterName, index);
	}

}