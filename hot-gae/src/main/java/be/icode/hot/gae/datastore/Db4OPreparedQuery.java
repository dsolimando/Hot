package be.icode.hot.gae.datastore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;

public class Db4OPreparedQuery implements PreparedQuery {
	
	private Query query;
	
	private ObjectContainer objectContainer;
	
	private FetchOptions defaultFetchOptions = FetchOptions.Builder.withDefaults();
	
	public Db4OPreparedQuery(Query query, ObjectContainer objectContainer) {
		this.query = query;
		this.objectContainer = objectContainer;
	}

	@Override
	public Iterable<Entity> asIterable() {
		return asIterable(defaultFetchOptions);
	}

	@Override
	public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
		return asList(fetchOptions);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean evaluateFilterPredicate (Filter filter, Entity entity) {
		if (filter == null) {
			return true;
		}
		if (filter instanceof Query.FilterPredicate) {
			FilterPredicate filterPredicate = (FilterPredicate) filter;
			if (filterPredicate.getPropertyName().equals("key") || (entity.getProperty(filterPredicate.getPropertyName()) instanceof Comparable
					&& filterPredicate.getValue() instanceof Comparable)) {
				Comparable cp1;
				if (filterPredicate.getPropertyName().equals("key")) {
					if (entity.getKey().getId() > Integer.MAX_VALUE)
						cp1 = new Long(entity.getKey().getId());
					else
						cp1 = new Integer((int)entity.getKey().getId());
				}
				else cp1 = (Comparable) entity.getProperty(filterPredicate.getPropertyName());
				Comparable cp2 = (Comparable) filterPredicate.getValue();
				
				switch (filterPredicate.getFilterOperator()) {
				case EQUAL:
					return cp1.compareTo(cp2) == 0;
				case NOT_EQUAL:
					return cp1.compareTo(cp2) != 0;
				case GREATER_THAN:
					return (cp1.compareTo(cp2) > 0);
				case LESS_THAN:
					return (cp1.compareTo(cp2) < 0);
				case GREATER_THAN_OR_EQUAL:
					int c = cp1.compareTo(cp2);
					return ( c == 0 || c > 0 );
				case LESS_THAN_OR_EQUAL:
					int c2 = cp1.compareTo(cp2);
					return ( c2 == 0 || c2 < 0 );
				default:
					break;
				}
			} else {
				throw new RuntimeException("Entities properties must implement Comparable interface");
			}
		} else {
			boolean total = true;
			CompositeFilter compositeFilter = (CompositeFilter) filter;
			for (Filter subFilter : compositeFilter.getSubFilters()) {
				if (compositeFilter.getOperator() ==  CompositeFilterOperator.AND) {
					total &= evaluateFilterPredicate(subFilter, entity);
				} else {
					total |= evaluateFilterPredicate(subFilter, entity);
				}
			}
			return total;
		}
		return true;
	}
	
	@Override
	public List<Entity> asList(FetchOptions fetchOptions) {
		@SuppressWarnings("serial")
		List<Entity> entities = objectContainer.query(new Predicate<Entity>() {
			public boolean match(Entity entity) {
				// Only compare same kind of entities
				if (!entity.getKind().equals(query.getKind())) return false;
				return evaluateFilterPredicate(query.getFilter(), entity);
			}
		}, new Comparator<Entity>() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(Entity entity1, Entity entity2) {
				String propertyName;
				for (SortPredicate sortPredicate : query.getSortPredicates()) {
					propertyName = sortPredicate.getPropertyName();
					if (!entity1.getProperty(propertyName).equals(entity2.getProperty(propertyName))) {
						if (entity1.getProperty(propertyName) instanceof Comparable
								&& entity2.getProperty(propertyName) instanceof Comparable) {
							Comparable pc1 = (Comparable) entity1.getProperty(propertyName);
							Comparable pc2 = (Comparable) entity2.getProperty(propertyName);
							if (sortPredicate.getDirection() == SortDirection.ASCENDING)
								return pc1.compareTo(pc2);
							else
								return -pc1.compareTo(pc2);
						} else {
							throw new RuntimeException("Entities properties must implement Comparable interface");
						}
					}
				}
				return 0;
			}
		});
		// limit the result set
		if (fetchOptions.getOffset() >= entities.size())
			return new ArrayList<Entity>();
		if (fetchOptions.getOffset()+fetchOptions.getLimit() >= entities.size())
			return entities.subList(fetchOptions.getOffset(),entities.size());
		return entities.subList(fetchOptions.getOffset(), fetchOptions.getOffset()+fetchOptions.getLimit());
	}

	@Override
	public Entity asSingleEntity() {
		List<Entity> entities = asList(defaultFetchOptions);
		if (entities.size() > 1	)
			throw new TooManyResultsException();
		return entities.get(0);
	}

	@Override
	public int countEntities(FetchOptions fetchOptions) {
		List<Entity> entities = asList(fetchOptions);
		return entities.size();
	}
}
