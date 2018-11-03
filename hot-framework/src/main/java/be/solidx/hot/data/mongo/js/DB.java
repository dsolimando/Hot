package be.solidx.hot.data.mongo.js;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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

import com.mongodb.MongoClient;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.DBObjectNativeObjectTransformer;

import com.google.common.collect.Lists;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DB extends BasicDB<NativeObject> {
	
	public DB(String username, String password, String dbname, MongoClient mongo, DBObjectNativeObjectTransformer dbObjectTransformer) {
		super(username, password, dbname, mongo, dbObjectTransformer);
	}
	
	@Override
	public be.solidx.hot.data.mongo.Collection<NativeObject> getCollection(String name) {
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
