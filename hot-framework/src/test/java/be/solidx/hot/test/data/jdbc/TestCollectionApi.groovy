package be.solidx.hot.test.data.jdbc

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
 
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import be.solidx.hot.data.jdbc.groovy.DB
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
class TestCollectionApi {

	@Autowired
	DB db

	@Test
	void testMetadata () {
		assert db.getCollectionMetadata("vets").columns == [
			"id",
			"first_name",
			"last_name"
		]
		assert db.getCollectionMetadata("vets").primaryKeys == ["id"]
		assert db.getCollectionMetadata("vets").relations == ["vet_specialties"]
		assert db.getCollectionMetadata("specialties").columns == [
			"id",
			"name",
		]
		assert db.getCollectionMetadata("specialties").primaryKeys == ["id"]
		assert db.getCollectionMetadata("specialties").relations == ["vet_specialties"]
		assert (db.getCollectionMetadata("vet_specialties").columns - [
			"specialty_id",
			"vet_id",
		] == [])
		assert db.getCollectionMetadata("vet_specialties").primaryKeys == []
		assert db.getCollectionMetadata("vet_specialties").relations == ["specialties","vets"]
		assert db.getCollectionMetadata("types").columns == [
			"id",
			"name",
		]
		assert db.getCollectionMetadata("types").primaryKeys == ["id"]
		assert db.getCollectionMetadata("types").relations == ["pets"]
		assert db.getCollectionMetadata("owners").columns == [
			"id",
			"first_name",
			"last_name",
			"address",
			"city",
			"telephone",
		]
		assert db.getCollectionMetadata("owners").primaryKeys == ["id"]
		assert db.getCollectionMetadata("owners").relations == ["pets"]
		assert db.getCollectionMetadata("pets").columns == [
			"id",
			"name",
			"birth_date",
			"type_id",
			"owner_id",
		]
		assert db.getCollectionMetadata("pets").primaryKeys == ["id"]
		assert db.getCollectionMetadata("pets").relations == ["visits","owners","types"]
		assert db.getCollectionMetadata("visits").columns == [
			"id",
			"pet_id",
			"visit_date",
			"description",
		]
		assert db.getCollectionMetadata("visits").primaryKeys == ["id"]
		assert db.getCollectionMetadata("visits").relations == ["pets"]
	}

	@Test
	void testFindAll () {
		def l = db.owners.find().toList()
		assert l.size() == 10
		assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
		assert l[5] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
		assert l[9] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
	}

	@Test
	void testFindWhere1 () {
		def l = db.owners.find([id:6]).toList()
		assert l.size() == 1
		assert l[0] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
	}

	@Test
	void testFindWhere2 () {
		def l = db.owners.find([id:6,first_name:"Jean"]).toList()
		assert l.size() == 1
		assert l[0] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
	}

	@Test
	void testFindWhere3 () {
		def l = db.owners.find([id:6,first_name:"Carlos"]).toList()
		assert l.size() == 0
	}

	@Test
	void testFindWhere4 () {
		def l = db.owners.find([$or:[
				[first_name:"Carlos"],
				[first_name:"George"]
			]]).toList()
		assert l.size() == 2
		assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
		assert l[1] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
	}

	@Test
	void testFindWhere5() {
		def l = db.owners.find([$or:[
				[first_name:"Carlos"],
				[first_name:"George"]
			], city:"Madison"]).toList()
		assert l.size() == 1
		assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
	}

	@Test
	void testFindWhereLike() {
		def l = db.owners.find([$or:[
				[first_name:[$like:"Carl%"]],
				[first_name:[$like:"%orge"]],
				[first_name:[$like:"%ett%"]]
			]]).sort([id:1]).toList()
		assert l.size() == 3
		assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
		assert l[1] == [id:2, first_name:"Betty", last_name:"Davis", address:"638 Cardinal Ave.", city:"Sun Prairie", telephone:"6085551749"]
		assert l[2] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
	}

	@Test
	void testFindWhereModulo() {
		def l = db.owners.find([$or:[
				[id:[$mod:[2, 0]]],
				[id:[$mod:[5, 0]]]
			]
		]).sort([id:1]).toList()
		print l
		assert l.size() == 6
		assert l[0].id == 2
		assert l[1].id == 4
		assert l[2].id == 5
		assert l[3].id == 6
		assert l[4].id == 8
		assert l[5].id == 10
	}

	@Test
	void testIn () {
		def l = db.owners.find([first_name:[$in:["George", "Jean", "Carlos"]]]).sort([id:1]).toList()
		assert l.size() == 3
		assert l[0] == [id:1, first_name:"George", last_name:"Franklin", address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"]
		assert l[1] == [id:6, first_name:"Jean", last_name:"Coleman", address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"]
		assert l[2] == [id:10, first_name:"Carlos", last_name:"Estaban", address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"]
	}

	@Test
	void testFindWhereModuloIn() {
		def l = db.owners.find([$or:[
				[id:[$mod:[2, 0]]],
				[id:[$mod:[5, 0]]]
			],
			first_name:[
				$in:[
					"Betty",
					"Peter",
					"Carlos"]
			]
		]).sort([id:1]).toList()
		assert l.size() == 3
		assert l[0].id == 2
		assert l[1].id == 5
		assert l[2].id == 10
	}

	@Test
	void testNin () {
		def l = db.owners.find([first_name:[$nin:["George", "Jean", "Carlos"]]]).sort([id:1]).toList()
		assert l.size() == 7
	}

	@Test
	void testFindJoinWhere1 () {
		def l = db.owners.join(["pets", "pets.types"]).find(["pets.name":"Leo","types.name":"cat"]).toList()
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
	}

	@Test
	void testJoinIn () {
		def l = db.owners.join(["pets", "pets.types"]).find(["types.name":[$in:["cat", "dog", "hamster"]]]).sort(["id":1,"pets.id":1]).toList()
		assert l.size() == 6
		assert l[0].first_name == "George" 	&& l[0].pets[0].type.name == "cat"
		assert l[1].first_name == "Betty" 	&& l[1].pets[0].type.name == "hamster"
		assert l[2].first_name == "Eduardo" && l[2].pets[0].type.name == "dog"
		assert l[2].first_name == "Eduardo" && l[2].pets[1].type.name == "dog"
		assert l[3].first_name == "Jean"	&& l[3].pets[0].type.name == "cat"
		assert l[3].first_name == "Jean" 	&& l[3].pets[1].type.name == "cat"
		assert l[4].first_name == "Maria" 	&& l[4].pets[0].type.name == "dog"
		assert l[5].first_name == "Carlos" 	&& l[5].pets[0].type.name == "dog"
		assert l[5].first_name == "Carlos" 	&& l[5].pets[1].type.name == "cat"
	}

	@Test
	void testJoinInModulo () {
		def l = db.owners
				.join(["pets", "pets.types"])
				.find(["types.name":[$in:["cat", "dog", "hamster"]],
					"pets.id":[$mod:[2, 0]]]).sort(["id":1]).toList()

		assert l.size() == 5
		assert l[0].first_name == "Betty" 	&& l[0].pets[0].type.name == "hamster" 	&& l[0].pets[0].id == 2
		assert l[1].first_name == "Eduardo" && l[1].pets[0].type.name == "dog" 		&& l[1].pets[0].id == 4
		assert l[2].first_name == "Jean"	&& l[2].pets[0].type.name == "cat" 		&& l[2].pets[0].id == 8
		assert l[3].first_name == "Maria" 	&& l[3].pets[0].type.name == "dog" 		&& l[3].pets[0].id == 10
		assert l[4].first_name == "Carlos" 	&& l[4].pets[0].type.name == "dog" 		&& l[4].pets[0].id == 12
	}

	@Test
	void testJoinInModuloLimit () {
		def l = db.owners
				.join(["pets", "pets.types"])
				.find(["types.name":[$in:["cat", "dog", "hamster"]],
					"pets.id":[$mod:[2, 0]]]).sort(["id":1]).limit(2).toList()

		assert l.size() == 2
		assert l[0].first_name == "Betty" 	&& l[0].pets[0].type.name == "hamster" 	&& l[0].pets[0].id == 2
		assert l[1].first_name == "Eduardo" && l[1].pets[0].type.name == "dog" 		&& l[1].pets[0].id == 4
	}

	@Test
	void testJoinInModuloLimitOffset () {
		def l = db.owners
				.join(["pets", "pets.types"])
				.find(["types.name":[$in:["cat", "dog", "hamster"]],
					"pets.id":[$mod:[2, 0]]]).sort(["id":1]).limit(1).skip(1).toList()

		assert l.size() == 1
		assert l[0].first_name == "Eduardo" && l[0].pets[0].type.name == "dog" 	&& l[0].pets[0].id == 4
	}
}
