package be.icode.hot.data.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.python.core.Py;
import org.python.core.PyObject;

import be.icode.hot.data.Cursor;
import be.icode.hot.data.DB;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

public class BasicDB<T extends Map<?,?>> implements DB<T> {

	protected Mongo mongo;
	
	String username;
	
	String password;
	
	protected String dbname;
	
	protected DBObjectTransformer<T> dbObjectTransformer;
	
	public BasicDB(String username, String password, String dbname, Mongo mongo, DBObjectTransformer<T> dbObjectTransformer) {
		this.mongo = mongo;
		this.username = username;
		this.password = password;
		this.dbname = dbname;
		this.dbObjectTransformer = dbObjectTransformer;
	}

	@Override
	public Collection<T> getCollection(String name) {
		com.mongodb.DB db = mongo.getDB(dbname);
		return new BasicCollection(db, name);
	}

	@Override
	public List<String> listCollections() {
		com.mongodb.DB db = mongo.getDB(dbname);
//		authenticate(db);
		return new ArrayList<String>(db.getCollectionNames());
	}

	@Override
	public List<String> getPrimaryKeys(String collection) {
		return Arrays.asList("_id");
	}
	
	protected void authenticate (com.mongodb.DB db) {
		if (username != null && !username.isEmpty() && password != null && password.isEmpty()) {
			db.authenticate(username, password.toCharArray());
		}
	}
	
	public PyObject __getattr__(String name) {
		return Py.java2py(getCollection(name));
	}
	
	public class BasicCollection implements Collection<T> {
		
		protected com.mongodb.DB db;
		
		protected String name;
		
		public BasicCollection(com.mongodb.DB db, String name) {
			this.db = db;
			this.name = name;
		}

		@Override
		public T findOne(T where) {
//			authenticate(db);
			transformId(where);
			return dbObjectTransformer.fromDBObject((BasicDBObject) db.getCollection(name).findOne(new BasicDBObject(where)));
		}

		@Override
		public Cursor<T> find(T where) {
			transformId(where);
			return buildCursor(where);
		}

		@Override
		public Cursor<T> find() {
			return buildCursor(null);
		}

		@Override
		public Collection<T> update(T values, T where) {
			transformId(where);
			BasicDBObject valuesDbObject = new BasicDBObject(values);
			BasicDBObject whereDbObject = new BasicDBObject(where);
			if (values.keySet().size() == 1 && values.keySet().contains("$set")) {
				db.getCollection(name).update(whereDbObject, valuesDbObject,false,true);
			} else {
				db.getCollection(name).update(whereDbObject, valuesDbObject);
			}
			return this;
		}

		@Override
		public Collection<T> remove(T where) {
			transformId(where);
			BasicDBObject whereDbObject = new BasicDBObject(where);
			db.getCollection(name).remove(whereDbObject);
			return this;
		}

		@Override
		public T insert(T values) {
			BasicDBObject valuesDbObject = new BasicDBObject(values);
			WriteResult wr = db.getCollection(name).insert(valuesDbObject);
			return dbObjectTransformer.put(values, "_id", valuesDbObject.get("_id").toString());
		}
		
		@Override
		public T save(T values) {
			BasicDBObject valuesDbObject = new BasicDBObject(values);
			WriteResult wr = db.getCollection(name).save(valuesDbObject);
			return dbObjectTransformer.put(values, "_id", valuesDbObject.get("_id").toString());
		}

		public DBCollection getMongoCollection() {
			return db.getCollection(name);
		}

		@Override
		public Collection<T> drop() {
			db.getCollection(name).drop();
			return this;
		}

		@Override
		public T findOne() {
			return dbObjectTransformer.fromDBObject((BasicDBObject) db.getCollection(name).findOne());
		}
		
		@Override
		public long count(T where) {
			if (where == null) {
				return db.getCollection(name).count();
			} else {
				transformId(where);
				return db.getCollection(name).count(new BasicDBObject(where));
			}
		}
		
		@Override
		public T runCommand(String command, T t) {
			if (command.equals("text")) {
				BasicDBObject basicDBObject = new BasicDBObject();
				basicDBObject.put(command, name);
				basicDBObject.putAll(t);
				CommandResult result = db.command(basicDBObject);
				return dbObjectTransformer.fromDBObject(result);
			} else {
				throw new RuntimeException("Command ["+command+"] actually not supported");
			}
		}
		
		protected Cursor<T> buildCursor (T where) {
			if (where == null) {
				return new BasicCursor(db.getCollection(name));
			} else {
				return new BasicCursor(db.getCollection(name),where);
			}
		}
		
		protected void transformId (T where) {
			Object id = where.get("_id");
			if (id != null) {
				if (id instanceof String) {
					System.out.println(id);
					dbObjectTransformer.put(where, "_id", new ObjectId((where.get("_id").toString())));
				}
			}
		}
	}
	
	public class BasicCursor implements Cursor<T> {
		
		private DBCursor dbCursor;
		
		public BasicCursor(DBCollection collection) {
			this.dbCursor = collection.find();
		}
		
		public BasicCursor(DBCollection collection, T whereMap) {
			this.dbCursor = collection.find(new BasicDBObject(whereMap));
		}

		@Override
		public Iterator<T> iterator() {
			List<T> results = new ArrayList<T>();
			for (DBObject dbObject : dbCursor) {
				BasicDBObject basicDBObject = (BasicDBObject) dbObject;
				results.add(dbObjectTransformer.fromDBObject(basicDBObject));
			}
			return results.iterator();
		}
		
		@Override
		public Integer count() {
			return dbCursor.count();
		}

		@Override
		public Cursor<T> limit(Integer limit) {
			dbCursor.limit(limit);
			return this;
		}

		@Override
		public Cursor<T> skip(Integer at) {
			dbCursor.limit(at);
			return this;
		}

		@Override
		public Cursor<T> sort(T sortMap) {
			dbCursor.sort(new BasicDBObject(sortMap));
			return this;
		}
	}
}
