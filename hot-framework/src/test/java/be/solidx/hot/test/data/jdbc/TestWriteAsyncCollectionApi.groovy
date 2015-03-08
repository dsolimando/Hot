package be.solidx.hot.test.data.jdbc;

import static org.junit.Assert.*

import java.util.concurrent.Executors

import org.junit.Test

import be.solidx.hot.data.jdbc.groovy.GroovyAsyncDBProxy

class TestWriteAsyncCollectionApi extends TestWriteCollectionApi {

	def eventLoop = Executors.newFixedThreadPool(1)
	def blockingThreadPool = Executors.newFixedThreadPool(10)
	
	@Test
	void testAsyncUpdate () {
		
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		asyncDb.visits.update ([visit_date:"2012-09-04"],[id:2,pet_id:8,visit_date:"1996-03-04",description:"rabies shot"])
			.done {
				db.visits.find([visit_date:"2012-09-04"]).toList().size() == 1
				println "done"
			}
	}
}
