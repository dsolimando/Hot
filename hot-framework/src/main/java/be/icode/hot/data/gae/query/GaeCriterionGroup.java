package be.icode.hot.data.gae.query;

import java.util.ArrayList;
import java.util.List;

import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.criterion.Operator;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;

public class GaeCriterionGroup implements GaeCriterion {

	private CompositeFilter compositeFilter;
	
	public GaeCriterionGroup(Operator operator, List<? extends Criterion> criteria) {
		List<Filter> filterPredicates = new ArrayList<Query.Filter>();
		for (Criterion criterion : criteria) {
			filterPredicates.add(((GaeCriterion) criterion).getFilter());
		}
		switch (operator) {
		case $or:
			compositeFilter = new CompositeFilter(CompositeFilterOperator.OR, filterPredicates);
			break;

		default:
			compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filterPredicates);
		}
	}
	
	@Override
	public CompositeFilter getFilter() {
		return compositeFilter;
	}
}
