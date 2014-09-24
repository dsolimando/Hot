package be.icode.hot.gae.test;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.icode.hot.gae.datastore.Db4ODataStore;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

public class TestDb4oDatastrore {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	@Before
	public void createEntities() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		Db4ODataStore dataStore = new Db4ODataStore(container);
		
		// Store first entity
		Key key = KeyFactory.createKey ("personnes",1l);
		Entity entity = new Entity(key);
		entity.getProperties().put("name", "damien");
		entity.getProperties().put("age", 29);
		entity.getProperties().put("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.001"));
		dataStore.put(entity);
		
		// Store second entity
		key = KeyFactory.createKey ("personnes",2l);
		entity = new Entity(key);
		entity.getProperties().put("name", "julie");
		entity.getProperties().put("age", 27);
		entity.getProperties().put("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.001"));
		dataStore.put(entity);
		container.close();
	}
	
	@Test
	public void testRetrieveEntityEqual() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		
		try {
			// Equal Test String
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("name", "damien", FilterOperator.EQUAL));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			// Equal Test Date
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.001"),FilterOperator.EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testRetrieveEntityLT() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		
		try {
			// Lower then Test int
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 29, FilterOperator.LESS_THAN));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("julie", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 26, FilterOperator.LESS_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
			
			// Lower then Test Date
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.002"),FilterOperator.LESS_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:29:59.999"), FilterOperator.LESS_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testRetrieveEntityLTE() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		
		try {
			// Lower then or equal Test integer
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 28, FilterOperator.LESS_THAN_OR_EQUAL));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("julie", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 29, FilterOperator.LESS_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 26, FilterOperator.LESS_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
			
			// Lower then or equal Test Date
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.002"),FilterOperator.LESS_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.001"),FilterOperator.LESS_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:29:59.999"),FilterOperator.LESS_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testRetrieveEntityGTE() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		
		try {
			// Greater then or equal Test
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 29, FilterOperator.GREATER_THAN_OR_EQUAL));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 27, FilterOperator.GREATER_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 30, FilterOperator.GREATER_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
			
			// Greater then or equal Test Date
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:29:59.999"),FilterOperator.GREATER_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.001"), FilterOperator.GREATER_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("julie", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.003"), FilterOperator.GREATER_THAN_OR_EQUAL));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testRetrieveEntityGT() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		
		try {
			// Greater then Test
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 28, FilterOperator.GREATER_THAN));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 30, FilterOperator.GREATER_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
			
			// Greater then or equal Test Date
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:29:59.999"),FilterOperator.GREATER_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.000"), FilterOperator.GREATER_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(1, entities.size());
			Assert.assertEquals("julie", entities.get(0).getProperty("name"));
			
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.001"), FilterOperator.GREATER_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(0, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testMultipleFilters() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		try {
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			CompositeFilter compositeFilter = CompositeFilterOperator.and(Arrays.<Filter>asList(
					new Query.FilterPredicate("age", 27, FilterOperator.EQUAL),
					new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1983-02-06 23:30:00.001"),FilterOperator.EQUAL)));
			
			CompositeFilter compositeFilter2 = CompositeFilterOperator.and(Arrays.<Filter>asList(
					new Query.FilterPredicate("age", 29, FilterOperator.EQUAL),
					new Query.FilterPredicate("birthday", DATE_FORMAT.parseObject("1981-12-24 11:30:00.001"),FilterOperator.EQUAL)));
			
			query.setFilter(CompositeFilterOperator.or(Arrays.<Filter>asList(compositeFilter,compositeFilter2)));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testFetchOptions() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		try {
			// Limit 1 result
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 26, FilterOperator.GREATER_THAN));
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults().limit(1));
			Assert.assertEquals(1, entities.size());
			
			// Offset 1
			query = new Query("personnes");
			query.setFilter(new Query.FilterPredicate("age", 26, FilterOperator.GREATER_THAN));
			pquery = dataStore.prepare(query);
			entities = pquery.asList(FetchOptions.Builder.withDefaults().offset(1));
			Assert.assertEquals(1, entities.size());
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testSortDesc() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		try {
			// Limit 1 result
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.addSort("age", SortDirection.DESCENDING);
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			Assert.assertEquals("damien", entities.get(0).getProperty("name"));
		} finally {
			container.close();
		}
	}
	
	@Test
	public void testSortAsc() throws Exception {
		ObjectContainer container = Db4oEmbedded.openFile(Db4oEmbedded
		        .newConfiguration(),"/tmp/db4o_db");
		try {
			// Limit 1 result
			Db4ODataStore dataStore = new Db4ODataStore(container);
			Query query = new Query("personnes");
			query.addSort("age", SortDirection.ASCENDING);
			PreparedQuery pquery = dataStore.prepare(query);
			List<Entity> entities = pquery.asList(FetchOptions.Builder.withDefaults());
			Assert.assertEquals(2, entities.size());
			Assert.assertEquals("julie", entities.get(0).getProperty("name"));
		} finally {
			container.close();
		}
	}
	
	
	@After
	public void deleteDB () {
		new File("/tmp/db4o_db").delete();
	}
}
