package com.google.appengine.api.datastore;

import java.util.ArrayList;
import java.util.List;

public class Query {

	private String kind;
	
	private List<FilterPredicate> filterPredicates = new ArrayList<Query.FilterPredicate>();
	
	private List<SortPredicate> sortPredicates = new ArrayList<SortPredicate>();
	
	private Filter filter;
	
	private boolean keysOnly = false;
	
	public Query(String kind) {
		this.kind = kind;
	}
	
	public Query addSort (String propertyName) {
		addSort(propertyName, SortDirection.ASCENDING);
		return this;
	}
	
	public Query addSort (String propertyName, SortDirection sortDirection) {
		SortPredicate sortPredicate = new SortPredicate(propertyName, sortDirection);
		sortPredicates.add(sortPredicate);
		return this;
	}
	
	public List<FilterPredicate> getFilterPredicates() {
		return filterPredicates;
	}
	
	public List<SortPredicate> getSortPredicates() {
		return sortPredicates;
	}
	
	public String getKind() {
		return kind;
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	public void setKeysOnly() {
		this.keysOnly = true;
	}
	
	public static enum FilterOperator {
		EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL,
		IN,
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		NOT_EQUAL
	}
	
	public interface Filter {
	}
	
	public static class FilterPredicate implements Filter{
		private String propertyName;
		
		private Object value;
		
		private FilterOperator filterOperator = FilterOperator.EQUAL;

		public FilterPredicate(String propertyName, Object value, FilterOperator filterOperator) {
			super();
			this.propertyName = propertyName;
			this.value = value;
			this.filterOperator = filterOperator;
		}
		
		public FilterOperator getFilterOperator() {
			return filterOperator;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
		
		public Object getValue() {
			return value;
		}
	}
	
	public static class CompositeFilter implements Filter {
		CompositeFilterOperator operator;
		List<Filter> subFilters = new ArrayList<Query.Filter>();

		public CompositeFilter(final CompositeFilterOperator compositeFilteroperator, final List<Filter> filterPredicates) {
			this.operator = compositeFilteroperator;
			this.subFilters.addAll(filterPredicates);
		}
		
		public CompositeFilterOperator getOperator() {
			return operator;
		}
		
		public List<Filter> getSubFilters() {
			return subFilters;
		}
	}
	
	public static enum CompositeFilterOperator {
		AND,OR;
		
		public static CompositeFilter or (List<Filter> subFilters) {
			return new CompositeFilter(CompositeFilterOperator.OR, subFilters);
		}
		
		public static CompositeFilter and (List<Filter> subFilters) {
			return new CompositeFilter(CompositeFilterOperator.AND, subFilters);
		}
	}
	
	public static class SortPredicate {
		private String propertyName;
		
		private SortDirection direction;

		public SortPredicate(String propertyName, SortDirection sortDirection) {
			super();
			this.propertyName = propertyName;
			this.direction = sortDirection;
		}
		
		public SortDirection getDirection() {
			return direction;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
	}
	
	public enum SortDirection {
		ASCENDING,
		DESCENDING 
	}
}
