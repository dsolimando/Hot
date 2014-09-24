package be.icode.hot.data.jdbc;

import java.util.List;

import be.icode.hot.data.criterion.AbstractCriterionFactory;
import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.criterion.Operator;
import be.icode.hot.data.jdbc.sql.criterion.CriterionGroup;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.criterion.ModuloCriterion;
import be.icode.hot.data.jdbc.sql.criterion.MultivaluedCriterion;
import be.icode.hot.data.jdbc.sql.criterion.SqlCriterion;

public abstract class AbstractSqlCriterionFactory extends AbstractCriterionFactory<Criterion> {

	@Override
	protected Criterion buildCriterion(Operator operator, String parameterName, Object value, int index) {
		return new CriterionImpl(operator, parameterName, index);
	}
	
	@Override
	protected Criterion buildCriterionGroup(Boolean and, List<? extends Criterion> criterion) {
		return new CriterionGroup(and, criterion);
	}

	@Override
	protected Criterion buildMultivaluedCriterion(Operator operator, String parameterName, List<?> values, int index) {
		return new MultivaluedCriterion(operator, parameterName , index, values.size());
	}

	@Override
	protected Criterion buildNativeCriterion(String expression) {
		return new SqlCriterion(expression);
	}
	
	@Override
	protected Criterion buildModuloCriterion(String parameterName, int moduloValue, int moduloResult, int index) {
		return new ModuloCriterion(parameterName, index);
	}
}
