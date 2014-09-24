package be.icode.hot.data.gae.query;

import be.icode.hot.data.criterion.Criterion;

import com.google.appengine.api.datastore.Query.Filter;

public interface GaeCriterion extends Criterion{

	Filter getFilter();
}