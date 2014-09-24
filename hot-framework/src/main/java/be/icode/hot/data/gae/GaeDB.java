package be.icode.hot.data.gae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import be.icode.hot.data.Collection;
import be.icode.hot.data.DB;
import be.icode.hot.data.gae.query.util.EntityTransformer;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("rawtypes")
public class GaeDB<T extends Map> implements DB<T> {

	private GaeCollectionFactory<T> collectionFactory;
	
	private DatastoreService datastoreService;
	
	public GaeDB(GaeCollectionFactory<T> collectionFactory, DatastoreService datastoreService) {
		this.collectionFactory = collectionFactory;
		this.datastoreService = datastoreService;
	}

	@Override
	public Collection<T> getCollection(String kind) {
		return collectionFactory.buildGaeCollection(kind);
	}
	
	@Override
	public List<String> getPrimaryKeys(String collection) {
		return Arrays.asList(EntityTransformer.ID);
	}
	
	public List<String> listCollections() {
		Query query = new Query (Entities.KIND_METADATA_KIND);
		ArrayList<String> collections = new ArrayList<String>();
		for (Entity entity : datastoreService.prepare(query).asIterable()) {
			collections.add(entity.getKey().getName());
		}
		return collections;
	}
}
