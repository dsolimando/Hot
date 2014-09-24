package be.icode.hot.data.gae;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.icode.hot.data.Collection;
import be.icode.hot.data.Cursor;
import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.gae.query.GaeCriterion;
import be.icode.hot.data.gae.query.util.EntityTransformer;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("rawtypes")
public class GaeCollection<T extends Map> implements Collection<T> {

	private static final Log logger = LogFactory.getLog(GaeCollection.class);

	private String kind;

	private DatastoreService datastoreService;

	private EntityTransformer<T> entityToMapTransformer;

	private CriterionFactory criterionFactory;

	public GaeCollection(String kind, DatastoreService datastoreService, EntityTransformer<T> entityToMapTransformer, CriterionFactory criterionFactory) {
		this.kind = kind;
		this.datastoreService = datastoreService;
		this.entityToMapTransformer = entityToMapTransformer;
		this.criterionFactory = criterionFactory;
	}

	@Override
	public T findOne(T criteria) {
		Entity entity = entityToMapTransformer.fromMap(criteria, kind);
		entity = datastoreService.get(entity.getKey());
		return entityToMapTransformer.toMap(entity);
	}

	@Override
	public Cursor<T> find(T criteria) {
		Query query = new Query(kind);
		GaeCriterion gaeCriterion = (GaeCriterion) criterionFactory.buildCriteria(criteria);
		query.setFilter(gaeCriterion.getFilter());
		return new GaeCursor<T>(query, datastoreService, entityToMapTransformer);
	}

	@Override
	public Cursor<T> find() {
		return new GaeCursor<T>(new Query(kind), datastoreService, entityToMapTransformer);
	}

	@Override
	public long count(Map criteria) {
		Query query = new Query(kind);
		query.setKeysOnly();
		List<Entity> all = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());
		return all.size();
	}

	@Override
	public Collection<T> update(Map values, Map criteria) {
		Query query = new Query(kind);
		GaeCriterion gaeCriterion = (GaeCriterion) criterionFactory.buildCriteria(criteria);
		query.setFilter(gaeCriterion.getFilter());
		List<Entity> entities = datastoreService.prepare(query).asList(new FetchOptions());
		for (Entity entity : entities) {
			for (Object keyObject : values.keySet()) {
				if (keyObject instanceof String) {
					entity.setProperty((String) keyObject, values.get(keyObject));
				} else {
					logger.error("Update map must contain only String keys");
				}
			}
		}
		datastoreService.put(entities);
		return this;
	}

	@Override
	public Collection<T> remove(T entity) {
		datastoreService.delete(entityToMapTransformer.fromMap(entity, kind).getKey());
		return this;
	}

	@Override
	public T insert(T entity) {
		Entity gaeEntity = entityToMapTransformer.fromMap(entity, kind);
		Entity inserted = new Entity(datastoreService.put(gaeEntity));
		inserted.getProperties().putAll(gaeEntity.getProperties());
		return entityToMapTransformer.toMap(inserted);
	}

	@Override
	public Collection<T> drop() {
		Query query = new Query(kind);
		query.setKeysOnly();
		List<Entity> all = datastoreService.prepare(query).asList(new FetchOptions());
		List<Key> keys = new ArrayList<Key>();
		for (Entity entity : all) {
			keys.add(entity.getKey());
		}
		datastoreService.delete(keys);
		return this;
	}
}
