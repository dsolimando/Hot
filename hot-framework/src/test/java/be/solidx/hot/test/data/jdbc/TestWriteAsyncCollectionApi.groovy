package be.solidx.hot.test.data.jdbc;

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
