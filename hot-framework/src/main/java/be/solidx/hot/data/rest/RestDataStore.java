package be.solidx.hot.data.rest;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.impl.DefaultDeferredManager;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import be.solidx.hot.data.Collection;
import be.solidx.hot.data.Cursor;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.rest.RestController;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;

@Controller
public class RestDataStore extends RestController {

	protected static final int FIND_ALL_MAX_RESULTS 	= 1000;
	protected static final String PAGE 				= "page";
	protected static final String SIZE 				= "size";
	protected static final String CONTENT 			= "content";
	protected static final String LINKS 			= "links";
	protected static final String SEARCH 			= "search";
	protected static final String REL 				= "rel";
	protected static final String HREF 				= "href";
	protected static final String SELF				= "self";
	protected static final String LOCATION			= "Location";
	
	protected Map<String, DB<Map<String, Object>>> dbMap;
	
	private ConversionService conversionService;
	
	private GroovyMapConverter groovyDataConverter;
	
	private ExecutorService blockingTaskThreadPool;
	
	public RestDataStore(Map<String, DB<Map<String, Object>>> dbMap, ConversionService conversionService, GroovyMapConverter groovyDataConverter,
			ExecutorService blockingTaskThreadPool) {
		this.dbMap = dbMap;
		this.conversionService = conversionService;
		this.groovyDataConverter = groovyDataConverter;
		this.blockingTaskThreadPool = blockingTaskThreadPool;
	}

	@RequestMapping(value = "/{dbname}", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> showTables (final HttpServletRequest httpRequest,
			@PathVariable final String dbname) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
				for (String collectionName : db.listCollections()) {
					Map<String, Object> entry = new HashMap<String, Object>();
					entry.put(REL, collectionName);
					entry.put(HREF, getDatastoreUri(httpRequest,dbname).path("/"+collectionName).build().toUri().toASCIIString());
					links.add(entry);
				}
				Map<String, Object> response = new HashMap<String, Object>();
				response.put(LINKS, links);
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> findAll(
			final HttpServletRequest httpRequest, 
			@PathVariable final String dbname,
			@PathVariable final String entity) {

		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				if (!db.listCollections().contains(entity)) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				int page = handleIntegerParam(httpRequest, PAGE);
				int size = handleIntegerParam(httpRequest, SIZE);

				// Retrieving Data
				Cursor<Map<String, Object>> cursor = db.getCollection(entity).find();
				if (page == 0)
					page = 1;
				if (size <= 1)
					size = FIND_ALL_MAX_RESULTS;
				cursor.limit(size);
				cursor.skip((page -1) * size);
				ArrayList<Map<String, Object>> results = Lists.newArrayList(cursor);
				Map<String, Object> response = new LinkedHashMap<String, Object>();
				response.put(entity, results);

				// Adds links
				Map<String, String> link = new HashMap<String, String>();
				link.put("rel", String.format("%s.search", entity));
				link.put("href", getDatastoreUri(httpRequest,dbname).path("/"+entity).path("/"+SEARCH).build().toUri().toASCIIString());
				response.put(LINKS, link);
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/{ids}", method = RequestMethod.DELETE)
	synchronized public DeferredResult<ResponseEntity<byte[]>> delete (
			final HttpServletRequest httpRequest,
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@PathVariable final String ids) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				if (!db.listCollections().contains(entity)) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				Collection<Map<String, Object>> entityCollection = db.getCollection(entity);
				
				// Construct critria
				Map<String, Object> criteria = new LinkedHashMap<String, Object>();
				String[] splittedIds = ids.split(",");
				for (int i = 0; i < splittedIds.length; i++) {
					criteria.put(db.getPrimaryKeys(entity).get(i), convert(splittedIds[i]));
				}
				// Retrieve data
				Map<String, ?> response = entityCollection.findOne(criteria);
			
				if (response == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				} else {
					entityCollection.remove(criteria);
					return buildEmptyResponse(HttpStatus.NO_CONTENT);
				}
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/search", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> seach(
			final HttpServletRequest httpRequest, 
			final UriComponentsBuilder uriBuilder,
			@PathVariable final String dbname,
			@PathVariable final String entity) {
		
		Callable<ResponseEntity<byte[]>> blocking  = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				int page = handleIntegerParam(httpRequest, PAGE);
				int size = handleIntegerParam(httpRequest, SIZE);
				
				Map<String, Object> criteria = new HashMap<String, Object>();
				criteria.remove(SIZE);
				criteria.remove(PAGE);
				Map<?, ?> tMap = groovyDataConverter.toMap(httpRequest.getParameterMap());
				for (Object key : tMap.keySet()) {
					if (tMap.get(key) != null) {
						criteria.put((String) key, convert(tMap.get(key)));
					}
				}
				
				// Retrieving Data
				Cursor<Map<String, Object>> cursor = db.getCollection(entity).find(criteria);
				if (page == 0)
					page = 1;
				if (size <= 1)
					size = FIND_ALL_MAX_RESULTS;
				cursor.limit(size);
				cursor.skip((page -1) * size);
				
				ArrayList<Map<String, Object>> results = Lists.newArrayList(cursor);
				Map<String, Object> response = new LinkedHashMap<String, Object>();
				response.put(CONTENT, results);
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/{ids}/{relation}", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> findByIdWithRelationLinks (
			final HttpServletRequest httpRequest, 
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@PathVariable final String ids,
			@PathVariable final String relation) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				if (!(dbMap.get(dbname) instanceof be.solidx.hot.data.jdbc.groovy.DB)) {
					return buildEmptyResponse(HttpStatus.NOT_IMPLEMENTED);
				}
				
				be.solidx.hot.data.jdbc.groovy.DB db = (be.solidx.hot.data.jdbc.groovy.DB) dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				TableMetadata entityCollectionMetadata = (TableMetadata) db.getCollectionMetadata(entity);
				TableMetadata relationCollectionMetadata = (TableMetadata) db.getCollectionMetadata(relation);
				if (entityCollectionMetadata == null || relationCollectionMetadata == null ) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				// Construct critria
				Map<String, Object> criteria = new LinkedHashMap<String, Object>();
				String[] splittedIds = ids.split(",");
				for (int i = 0; i < splittedIds.length; i++) {
					criteria.put(entity + "." + entityCollectionMetadata.getPrimaryKeys().get(i), convert(splittedIds[i]));
				}
				
				// Retrieve data
				Cursor<Map<String, Object>>	cursor = db.getCollection(relation).join(Lists.newArrayList(entity)).find(criteria);
				ArrayList<Map<String, Object>> results = Lists.newArrayList(cursor);
				Map<String, Object> response = new LinkedHashMap<String, Object>();
				
				// Add links
				List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
				for (Map<String, Object> result : results) {
					// Compute id
					String pk = "";
					String separator = "";
					for (String key : relationCollectionMetadata.getPrimaryKeys()) {
						pk += String.format("%s%s", separator,result.get(key));
						separator = ",";
					}
					// Add link
					Map<String, Object> entry = new HashMap<String, Object>();
					entry.put(REL, String.format("%s.%s[%s]", entity,relation,pk));
					entry.put(HREF, getDatastoreUri(httpRequest,dbname)
							.path("/"+entity).path("/"+ids).path("/"+relation).path("/"+pk)
							.build().toUri().toASCIIString());
					links.add(entry);
				}
				response.put(relation, links);
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/{ids}/{relation}/{relationIds}", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> findByIdWithRelation (
			final HttpServletRequest httpRequest, 
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@PathVariable final String ids,
			@PathVariable final String relation,
			@PathVariable final String relationIds) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				if (!(dbMap.get(dbname) instanceof be.solidx.hot.data.jdbc.groovy.DB)) {
					return buildEmptyResponse(HttpStatus.NOT_IMPLEMENTED);
				}
				be.solidx.hot.data.jdbc.groovy.DB db = (be.solidx.hot.data.jdbc.groovy.DB) dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				TableMetadata entityCollectionMetadata = (TableMetadata) db.getCollectionMetadata(entity);
				TableMetadata relationCollectionMetadata = (TableMetadata) db.getCollectionMetadata(relation);
				if (entityCollectionMetadata == null || relationCollectionMetadata == null ) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}

				// Construct critria
				Map<String, Object> criteria = new LinkedHashMap<String, Object>();
				String[] splittedIds = ids.split(",");
				String[] splittedRelationIds = relationIds.split(",");
				for (int i = 0; i < splittedIds.length; i++) {
					criteria.put(entity + "." + entityCollectionMetadata.getPrimaryKeys().get(i), convert(splittedIds[i]));
				}
				for (int i = 0; i < splittedRelationIds.length; i++) {
					criteria.put(relation + "." + relationCollectionMetadata.getPrimaryKeys().get(i), convert(splittedRelationIds[i]));
				}
				
				// Retrieve data
				Map<String, Object> response = db.getCollection(relation).join(Lists.newArrayList(entity)).findOne(criteria);
				if (response == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				// Add self link
				List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
				Map<String, Object> entry = new HashMap<String, Object>();
				entry.put(REL, SELF);
				entry.put(HREF, getDatastoreUri(httpRequest,dbname).path("/"+relation).path("/"+relationIds).build().toUri().toASCIIString());
				links.add(entry);
				response.put(LINKS, links);
				
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/{ids}", method = RequestMethod.GET)
	synchronized public DeferredResult<ResponseEntity<byte[]>> findById (
			final HttpServletRequest httpRequest,
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@PathVariable final String ids) {

		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				if (!db.listCollections().contains(entity)) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				// Construct critria
				Map<String, Object> criteria = new LinkedHashMap<String, Object>();
				String[] splittedIds = ids.split(",");
				for (int i = 0; i < splittedIds.length; i++) {
					criteria.put(db.getPrimaryKeys(entity).get(i), convert(splittedIds[i]));
				}
				
				// Retrieve data
				Map<String, Object> response = db.getCollection(entity).findOne(criteria);
				if (response == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				// Add links
				if (db instanceof be.solidx.hot.data.jdbc.groovy.DB) {
					be.solidx.hot.data.jdbc.groovy.DB dbProxy = (be.solidx.hot.data.jdbc.groovy.DB) db;
					List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
					for (String collectionName : dbProxy.getCollectionMetadata(entity).getRelations()) {
						Map<String, Object> entry = new HashMap<String, Object>();
						entry.put(REL, collectionName);
						entry.put(HREF, getDatastoreUri(httpRequest,dbname).path("/"+entity).path("/"+ids).path("/"+collectionName).build().toUri().toASCIIString());
						links.add(entry);
					}
					response.put(LINKS, links);
				}
				return buildJSONResponse(response, HttpStatus.OK);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}", method = RequestMethod.POST)
	synchronized public DeferredResult<ResponseEntity<byte[]>> save (
			final HttpServletRequest httpRequest, 
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@RequestBody final String body) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				if (!db.listCollections().contains(entity)) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				
				// Filter unknown table columns from input JSON
				Map<String, Object> entityMap = readJson(body);
				
				be.solidx.hot.data.jdbc.groovy.DB dbProxy = null;
				if (db instanceof be.solidx.hot.data.jdbc.groovy.DB) {
					dbProxy = (be.solidx.hot.data.jdbc.groovy.DB) db;
				}
				Map<String, Object> filteredMap = null;
				if (dbProxy != null) {
					filteredMap = filterEntity(entityMap, dbProxy.getCollectionMetadata(entity).getColumns());
				} else {
					filteredMap = entityMap;
				}
				// Save data
				Map<String, Object> insertedEntity = db.getCollection(entity).insert(filteredMap);
				
				// build pks
				String ids = "";
				String separator = "";
				for (String pk : db.getPrimaryKeys(entity)) {
					ids += String.format("%s%s", separator,pk);
					separator = ",";
				}
				// response headers
				UriComponentsBuilder resourceUrl = getDatastoreUri(httpRequest,dbname).path("/"+entity).path("/"+ids);
				HttpHeaders headers = new HttpHeaders();
				headers.set(LOCATION, resourceUrl.build().toUri().toASCIIString());
				
				// build response
				if (dbProxy != null) {
					List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
					for (String collectionName : dbProxy.getCollectionMetadata(entity).getRelations()) {
						Map<String, Object> entry = new HashMap<String, Object>();
						entry.put(REL, collectionName);
						entry.put(HREF, getDatastoreUri(httpRequest,dbname).path("/"+entity).path("/"+ids).path("/"+collectionName).build().toUri().toASCIIString());
						links.add(entry);
					}
					insertedEntity.put(LINKS, links);
				}
				return buildJSONResponse(insertedEntity,headers, HttpStatus.CREATED);
			}
		};
		
		return blockingCall(blocking);
	}
	
	@RequestMapping(value = "/{dbname}/{entity}/{ids}", method = RequestMethod.PUT)
	synchronized public DeferredResult<ResponseEntity<byte[]>> update (
			final HttpServletRequest httpRequest,
			@RequestBody final String body,
			@PathVariable final String dbname,
			@PathVariable final String entity,
			@PathVariable final String ids) {
		
		Callable<ResponseEntity<byte[]>> blocking = new Callable<ResponseEntity<byte[]>>() {

			@Override
			public ResponseEntity<byte[]> call() throws Exception {
				DB<Map<String, Object>> db = dbMap.get(dbname);
				if (db == null) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				if (!db.listCollections().contains(entity)) {
					return buildEmptyResponse(HttpStatus.NOT_FOUND);
				}
				TableMetadata collectionMetadata = null;
				if (db instanceof be.solidx.hot.data.jdbc.groovy.DB) {
					collectionMetadata = ((be.solidx.hot.data.jdbc.groovy.DB)db).getCollectionMetadata(entity);
				}
				Collection<Map<String, Object>> entityCollection = db.getCollection(entity);
				
				Map<String, Object> entityMap = readJson(body);
				
				// Construct critria
				Map<String, Object> criteria = new LinkedHashMap<String, Object>();
				String[] splittedIds = ids.split(",");
				List<String> primaryKeys = db.getPrimaryKeys(entity);
				for (int i = 0; i < splittedIds.length; i++) {
					String key = primaryKeys.get(i);
					Object value = convert(splittedIds[i]);
					criteria.put(key,value);
					entityMap.remove(key);
				}
				// Retrieve data
				Map<String, Object> response = entityCollection.findOne(criteria);
				
				// Filter unknown table columns from input JSON
				if (collectionMetadata != null) {
					entityMap = filterEntity(entityMap, collectionMetadata.getColumns());
				}
				
				// Update or insert
				if (response == null) {
					entityCollection.insert(entityMap);
				} else {
					entityCollection.update(new LinkedHashMap<String, Object>(criteria), entityMap);
				}
				// Add links
				if (collectionMetadata != null) {
					List<Map<String, Object>> links = new ArrayList<Map<String,Object>>();
					for (String collectionName : collectionMetadata.getRelations()) {
						Map<String, Object> entry = new HashMap<String, Object>();
						entry.put(REL, collectionName);
						entry.put(HREF, getDatastoreUri(httpRequest,dbname).path("/"+entity).path("/"+ids).path("/"+collectionName).build().toUri().toASCIIString());
						links.add(entry);
					}
					entityMap.put(LINKS, links);
				}
				
				return buildJSONResponse(entityMap,HttpStatus.CREATED);
			}
		};
		
		return blockingCall(blocking);
	}

	protected int handleIntegerParam(HttpServletRequest httpRequest, String paramName) {
		String pageString = httpRequest.getParameter(paramName);
		if (pageString != null) {
			try {
				return Integer.parseInt(pageString);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}
	
	protected UriComponentsBuilder getDatastoreUri (HttpServletRequest httpRequest, String dbname) {
		return ServletUriComponentsBuilder.fromContextPath(httpRequest).path("/data").path("/"+dbname);
	}
	
	protected Object convert (Object value) {
		try {
			return conversionService.convert(value, Long.class);
		} catch (Exception e) {
			try {
				return conversionService.convert(value, Date.class);
			} catch (Exception e1) {
				try {
					return conversionService.convert(value, Boolean.class);
				} catch (Exception e2) {
					try {
						String decoded = URLDecoder.decode(value.toString(), "utf-8");
						if (decoded.startsWith("/") && decoded.endsWith("/")) {
							return new BasicDBObject("$regex", StringUtils.substring(decoded, 1, -1));
						} else {
							return decoded;
						}
					} catch (UnsupportedEncodingException e3) {
						return value.toString();
					}
				}
			}
		}
	}
	
	protected Map<String, Object> filterEntity(Map<String, Object> entity, List<String> referenceKeys) {
		// Filter unknown table columns from input JSON
		Map<String, Object> filteredMap = new LinkedHashMap<String, Object>();
		for (String key : entity.keySet()) {
			if (referenceKeys.contains(key)) {
				filteredMap.put(key, entity.get(key));
			}
		}
		return filteredMap;
	}
	
	private DeferredResult<ResponseEntity<byte[]>> blockingCall (final Callable<ResponseEntity<byte[]>> dbCall) {
		
		final DeferredResult<ResponseEntity<byte[]>> deferredResult = new DeferredResult<>();
		
		DeferredManager deferredManager = new DefaultDeferredManager(blockingTaskThreadPool);
		
		deferredManager.when(dbCall).done(new DoneCallback<ResponseEntity<byte[]>>() {
			@Override
			public void onDone(ResponseEntity<byte[]> result) {
				deferredResult.setResult(result);
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable e) {
				deferredResult.setErrorResult(
						new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
			
			}
		});
		return deferredResult;
	}
	
	protected String extractStackTrace (Throwable e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}
}
