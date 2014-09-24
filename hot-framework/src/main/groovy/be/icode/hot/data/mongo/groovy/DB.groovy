package be.icode.hot.data.mongo.groovy

import be.icode.hot.data.mongo.BasicDB
import be.icode.hot.data.mongo.DBObjectTransformer
import be.icode.hot.data.mongo.BasicDB.BasicCollection

import com.mongodb.Mongo


class DB extends BasicDB<Map<String, Object>> {
	
	public DB(String username, String password, String dbname, Mongo mongo, DBObjectTransformer<Map<String, Object>> dbObjectTransformer) {
		super (username, password, dbname, mongo, dbObjectTransformer)
	}
	
	def getProperty (String name) {
		getCollection name
	}
	
	class Collection extends BasicCollection {
		
		public Collection(com.mongodb.DB db, String name) {
			super(db, name);
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
