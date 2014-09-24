package be.icode.hot.spring.config;

import java.util.Map;

import org.mozilla.javascript.NativeObject;
import org.python.core.PyDictionary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import be.icode.hot.data.DB;
import be.icode.hot.data.gae.GaeCollectionFactory;
import be.icode.hot.data.gae.GaeDB;
import be.icode.hot.data.gae.query.GaeCriterionFactory;
import be.icode.hot.data.gae.query.util.EntityToMapTransformerImpl;
import be.icode.hot.data.gae.query.util.EntityToNativeObjectTransformer;
import be.icode.hot.data.gae.query.util.EntityToPydictionaryTransformer;

@Configuration @Lazy
public class GaeConfig {

	@Bean
	public GaeCriterionFactory gaeCriterionFactory () {
		return new GaeCriterionFactory();
	}
	
	@Bean
	public DatastoreService dataStoreService() {
		return DatastoreServiceFactory.getDatastoreService();
	}
	
	@Bean
	public DB<Map<String, Object>> groovyGaeDB () {
		GaeCollectionFactory<Map<String, Object>> collectionFactory = new GaeCollectionFactory<Map<String,Object>>(dataStoreService(), gaeCriterionFactory(), new EntityToMapTransformerImpl());
		return new GaeDB<Map<String,Object>>(collectionFactory, dataStoreService());
	}
	
	@Bean
	public DB<NativeObject> jsGaeDB () {
		GaeCollectionFactory<NativeObject> collectionFactory = new GaeCollectionFactory<NativeObject>(dataStoreService(), gaeCriterionFactory(), new EntityToNativeObjectTransformer());
		return new GaeDB<NativeObject>(collectionFactory, dataStoreService());
	}
	
	@Bean
	public DB<PyDictionary> pyGaeDB () {
		GaeCollectionFactory<PyDictionary> collectionFactory = new GaeCollectionFactory<PyDictionary>(dataStoreService(), gaeCriterionFactory(), new EntityToPydictionaryTransformer());
		return new GaeDB<PyDictionary>(collectionFactory, dataStoreService());
	}
}
