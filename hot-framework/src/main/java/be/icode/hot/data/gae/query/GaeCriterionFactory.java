package be.icode.hot.data.gae.query;

import java.util.List;

import be.icode.hot.data.criterion.AbstractCriterionFactory;
import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.criterion.CriterionCreationException;
import be.icode.hot.data.criterion.Operator;

public class GaeCriterionFactory extends AbstractCriterionFactory<GaeCriterion> {

	@Override
	protected GaeCriterion buildCriterion(Operator operator, String parameterName, Object parameterValue, int index) {
		return new GaeCriterionImpl(operator, parameterName, parameterValue);
	}
	
	@Override
	protected GaeCriterionGroup buildCriterionGroup(Boolean and, List<? extends Criterion> criteria) {
		return new GaeCriterionGroup(and?Operator.$and:Operator.$or, criteria);
	}

	@Override
	protected GaeCriterion buildModuloCriterion(String parameterName, int moduloValue, int value, int index) {
		throw new CriterionCreationException("Moudulo operations on appen engine are not implemented");
	}

	@Override
	protected GaeCriterion buildMultivaluedCriterion(Operator operator, String parameterName, List<?> values, int index) {
		return new GaeMultivaluedCriterion(operator, parameterName, values);
	}

	@Override
	protected GaeCriterion buildRegexpCriterion(String parameterName, String value, int index) {
		throw new CriterionCreationException("Regexp operations on appen engine are not implemented");
	}

	@Override
	protected GaeCriterion buildNativeCriterion(String criterion) {
		throw new CriterionCreationException("No native operation exists on App Engine");
	}
}
