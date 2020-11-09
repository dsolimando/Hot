package be.solidx.hot.data.mongo.groovy

import be.solidx.hot.data.mongo.BasicDB

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

import be.solidx.hot.data.mongo.BasicDB.BasicCollection
import be.solidx.hot.data.mongo.DBObjectTransformer
import com.mongodb.MongoClient

class DB extends BasicDB<Map<String, Object>> {
	
	DB(String username, String password, String dbname, MongoClient mongo, DBObjectTransformer<Map<String, Object>> dbObjectTransformer) {
		super (username, password, dbname, mongo, dbObjectTransformer)
	}
	
	def getProperty (String name) {
		getCollection name
	}
	
	class Collection extends BasicCollection {
		
		public Collection(com.mongodb.DB db, String name) {
			super(db, name)
		}
	
		def leftShift (Map entity) {
			super.insert entity
		}
		
		def leftShift (List<Map> entities) {
			entities.each {
				super.insert it
			}
		}
	}
}
