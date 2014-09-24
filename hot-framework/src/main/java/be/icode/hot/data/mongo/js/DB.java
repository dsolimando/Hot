package be.icode.hot.data.mongo.js;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.icode.hot.data.mongo.BasicDB;
import be.icode.hot.data.mongo.DBObjectNativeObjectTransformer;

import com.google.common.collect.Lists;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DB extends BasicDB<NativeObject> {
	
	public DB(String username, String password, String dbname, Mongo mongo, DBObjectNativeObjectTransformer dbObjectTransformer) {
		super(username, password, dbname, mongo, dbObjectTransformer);
	}
	
	@Override
	public be.icode.hot.data.mongo.Collection<NativeObject> getCollection(String name) {
		com.mongodb.DB db = mongo.getDB(dbname);
		return new JSCollection(db, name);
	}
	
	public class JSCollection extends BasicCollection {

		public JSCollection(com.mongodb.DB db, String name) {
			super(db, name);
		}
		
		@Override
		protected Cursor buildCursor(NativeObject where) {
			if (where == null) {
				return new Cursor(db.getCollection(name));
			} else {
				return new Cursor(db.getCollection(name), where);
			}
		}
	}
	
	public class Cursor extends BasicCursor {

		public Cursor(DBCollection collection, NativeObject whereMap) {
			super(collection, whereMap);
		}

		public Cursor(DBCollection collection) {
			super(collection);
		}

		public NativeArray toArray () {
			return new NativeArray(Lists.newArrayList(iterator()).toArray());
		}
	}
}
