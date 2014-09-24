package be.icode.hot.data.gae.query;

import java.util.ArrayList;
import java.util.List;

import be.icode.hot.data.criterion.Operator;

import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class GaeMultivaluedCriterion implements GaeCriterion {
	
	private Filter inFilterPredicate;
	
	public GaeMultivaluedCriterion(Operator operator, String parameterName, List<?> values) {
		switch (operator) {
		case $in:
			inFilterPredicate = new FilterPredicate(parameterName, values, FilterOperator.IN);
			break;
		case $nin:
			List<Filter> filterPredicates = new ArrayList<Filter>();
			for (Object value : values) {
				filterPredicates.add(new FilterPredicate(parameterName, value, FilterOperator.NOT_EQUAL));
			}
			inFilterPredicate = new CompositeFilter (CompositeFilterOperator.AND,filterPredicates);
		default:
			break;
		}
	}
	
	@Override
	public Filter getFilter() {
		return inFilterPredicate;
	}
}
