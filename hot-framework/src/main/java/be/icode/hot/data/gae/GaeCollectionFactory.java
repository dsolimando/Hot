package be.icode.hot.data.gae;

import java.util.Map;

import be.icode.hot.data.Collection;
import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.gae.query.util.EntityTransformer;

import com.google.appengine.api.datastore.DatastoreService;

public class GaeCollectionFactory<T extends Map<?,?>>{

	private DatastoreService datastoreService;
	
	private CriterionFactory criterionFactory;
	
	private EntityTransformer<T> entityTransformer;
	
	public GaeCollectionFactory(DatastoreService datastoreService, CriterionFactory criterionFactory, EntityTransformer<T> entityTransformer) {
		this.datastoreService = datastoreService;
		this.criterionFactory = criterionFactory;
		this.entityTransformer = entityTransformer;
	}

	public Collection<T> buildGaeCollection (String kind) {
		return new GaeCollection<T>(kind, datastoreService, entityTransformer, criterionFactory);
	}
}
