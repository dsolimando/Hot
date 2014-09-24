package be.icode.hot.data.gae;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.icode.hot.data.Cursor;
import be.icode.hot.data.gae.query.util.EntityTransformer;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

@SuppressWarnings("rawtypes")
public class GaeCursor<T extends Map> implements Cursor<T> {
	
	private static final Log logger = LogFactory.getLog(GaeCursor.class);

	private Query query;
	
	private FetchOptions fetchOptions = new FetchOptions();
	
	private DatastoreService datastoreService;
	
	private EntityTransformer<T> entityToMapTransformer;
	
	public GaeCursor(Query query, DatastoreService datastoreService, EntityTransformer<T> entityToMapTransformer) {
		this.query = query;
		this.datastoreService = datastoreService;
		this.entityToMapTransformer = entityToMapTransformer;
	}

	@Override
	public Iterator<T> iterator() {
		List<Entity> entities = datastoreService.prepare(query).asList(fetchOptions);
		List<T> results = new ArrayList<T>();
		for (Entity entity : entities) {
			results.add(entityToMapTransformer.toMap(entity));
		}
		return results.iterator();
	}

	@Override
	public Integer count() {
		return datastoreService.prepare(query).asList(fetchOptions).size();
	}

	@Override
	public Cursor<T> limit(Integer limit) {
		fetchOptions.limit(limit);
		return this;
	}

	@Override
	public Cursor<T> skip(Integer at) {
		fetchOptions.offset(at);
		return this;
	}

	@Override
	public Cursor<T> sort(T sortMap) {
		for (Object keyObject : sortMap.keySet()) {
			if (!(keyObject instanceof String)) {
				logger.error("Sort map must contain only String keys");
				continue;
			}
			String key = (String) keyObject;
			Object value = sortMap.get(key);
			if (!(value instanceof Integer)) {
				logger.error("Sort map must contain only -1 or 1 integer values");
				continue;
			}
			Integer intValue = (Integer) value;
			query.addSort(key, intValue == 1? SortDirection.ASCENDING:SortDirection.DESCENDING);
		}
		return this;
	}
}
