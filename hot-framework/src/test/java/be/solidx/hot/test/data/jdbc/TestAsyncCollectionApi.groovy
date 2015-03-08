package be.solidx.hot.test.data.jdbc;

import static org.junit.Assert.*

import java.util.concurrent.Executors

import org.junit.After;
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import be.solidx.hot.data.jdbc.groovy.GroovyAsyncDBProxy


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
class TestAsyncCollectionApi {

	@Autowired
	be.solidx.hot.data.jdbc.groovy.DB db
	
	def eventLoop = Executors.newFixedThreadPool(1)
	
	def blockingThreadPool = Executors.newFixedThreadPool(10)
	
	@Test
	void testMetadata () { 
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		assert asyncDb.getCollectionMetadata("vets").columns == [
			"id",
			"first_name",
			"last_name"
		]
		assert asyncDb.getCollectionMetadata("vets").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("vets").relations == ["vet_specialties"]
		assert asyncDb.getCollectionMetadata("specialties").columns == [
			"id",
			"name",
		]
		assert asyncDb.getCollectionMetadata("specialties").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("specialties").relations == ["vet_specialties"]
		assert (asyncDb.getCollectionMetadata("vet_specialties").columns - [
			"specialty_id",
			"vet_id",
		] == [])
		assert asyncDb.getCollectionMetadata("vet_specialties").primaryKeys == []
		assert asyncDb.getCollectionMetadata("vet_specialties").relations == ["specialties","vets"]
		assert asyncDb.getCollectionMetadata("types").columns == [
			"id",
			"name",
		]
		assert asyncDb.getCollectionMetadata("types").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("types").relations == ["pets"]
		assert asyncDb.getCollectionMetadata("owners").columns == [
			"id",
			"first_name",
			"last_name",
			"address",
			"city",
			"telephone",
		]
		assert asyncDb.getCollectionMetadata("owners").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("owners").relations == ["pets"]
		assert asyncDb.getCollectionMetadata("pets").columns == [
			"id",
			"name",
			"birth_date",
			"type_id",
			"owner_id",
		]
		assert asyncDb.getCollectionMetadata("pets").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("pets").relations == ["visits","owners","types"]
		assert asyncDb.getCollectionMetadata("visits").columns == [
			"id",
			"pet_id",
			"visit_date",
			"description",
		]
		assert asyncDb.getCollectionMetadata("visits").primaryKeys == ["id"]
		assert asyncDb.getCollectionMetadata("visits").relations == ["pets"]
	}

	@Test
	void testFindAll () {
		
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		def promise = asyncDb.owners.find().promise()
		
		promise.done { l ->
			println Thread.currentThread()
			assert l.size() == 10
			assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
			assert l[5] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
			assert l[9] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
		}
	}
	
	@Test
	void testFindWhere1 () {
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		def promise = asyncDb.owners.find([id:6]).promise()
		
		promise.done { l ->
			assert l.size() == 1
			assert l[0] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
		}
	}
	
	@Test
	void testFindWhereLike() {
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		def promise = asyncDb.owners.find([$or:[
				[first_name:[$like:"Carl%"]],
				[first_name:[$like:"%orge"]],
				[first_name:[$like:"%ett%"]]
			]]).sort([id:1]).promise()
		
		promise.done { l ->
			assert l.size() == 3
			assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
			assert l[1] == [id:2, first_name:"Betty", last_name:"Davis", address:"638 Cardinal Ave.", city:"Sun Prairie", telephone:"6085551749"]
			assert l[2] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
		}
	}
	
	@Test
	void testFindJoinWhere1 () {
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		def promise = asyncDb.owners.join(["pets", "pets.types"])
			.find(["pets.name":"Leo","types.name":"cat"]).promise()
		
		promise.done { l ->
			assert l.size() == 1
			assert l[0].id == 1
			assert l[0].first_name == "George"
			assert l[0].address == "110 W. Liberty St."
			assert l[0].last_name == "Franklin"
			assert l[0].telephone == "6085551023"
			assert l[0].city == "Madison"
			assert l[0].pets.size() == 1
			assert l[0].pets[0].id == 1
			assert l[0].pets[0].name == "Leo"
			assert l[0].pets[0].type != null
			assert l[0].pets[0].type.id == 1
			assert l[0].pets[0].type.name == "cat"
			println "done"
		}.fail { error ->
			error.printStackTrace()
		}
//		sleep 5000
	}
	
	@Test
	void testJoinInModuloLimit () {
		
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		def promise = asyncDb.owners
				.join(["pets", "pets.types"])
				.find(["types.name":[$in:["cat", "dog", "hamster"]],
					"pets.id":[$mod:[2, 0]]]).sort(["id":1]).limit(2).promise()

		promise.done { l->
			assert l.size() == 2
			assert l[0].first_name == "Betty" 	&& l[0].pets[0].type.name == "hamster" 	&& l[0].pets[0].id == 2
			assert l[1].first_name == "Eduardo" && l[1].pets[0].type.name == "dog" 		&& l[1].pets[0].id == 4
			println "done"
		}
	}
	
	@Test
	void testJoinInModuloLimitOffset () {
		
		def asyncDb = new GroovyAsyncDBProxy(db, blockingThreadPool, eventLoop)
		
		def promise = asyncDb.owners
				.join(["pets", "pets.types"])
				.find(["types.name":[$in:["cat", "dog", "hamster"]],
					"pets.id":[$mod:[2, 0]]]).sort(["id":1]).limit(1).skip(1).promise()

		promise.done { l->
			assert l.size() == 1
			assert l[0].first_name == "Eduardo" && l[0].pets[0].type.name == "dog" 	&& l[0].pets[0].id == 4
		}	
	}
}
