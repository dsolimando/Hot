package be.icode.hot.data.gae.query;

import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.criterion.Operator;

import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class GaeCriterionImpl implements Criterion, GaeCriterion {

	private Filter filterPredicate;
	
	public GaeCriterionImpl(Operator operator, String parameterName, Object parameterValue) {
		filterPredicate = new FilterPredicate(parameterName, parameterValue, filterOperatorToOperator(operator));
	}
	
	@Override
	public Filter getFilter() {
		return filterPredicate;
	}
	
	public static final FilterOperator filterOperatorToOperator (Operator operator) {
		switch (operator) {
		case $eq:	return FilterOperator.EQUAL;
		case $gt:	return FilterOperator.GREATER_THAN;
		case $gte:	return FilterOperator.GREATER_THAN_OR_EQUAL;
		case $lt: 	return FilterOperator.LESS_THAN;
		case $lte:	return FilterOperator.LESS_THAN_OR_EQUAL;
		case $ne:	return FilterOperator.NOT_EQUAL;	
		default:
			return FilterOperator.EQUAL;
		}
	}
}
