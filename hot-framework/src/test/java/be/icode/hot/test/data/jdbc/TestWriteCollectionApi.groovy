package be.icode.hot.test.data.jdbc

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.icode.hot.data.jdbc.groovy.DB;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
class TestWriteCollectionApi  {

	@Autowired
	DB db

	@Before
	void testInsert1 () {
		def vet6 = [id:	6, first_name: "Sharon", last_name:	"Jenkins"]
		db.vets.insert([id:	1, first_name: "James", last_name:	"Carter"])
		db.vets.insert([id:	2, first_name: "Helen", last_name:	"Leary"])
		db.vets.insert([id:	3, first_name: "Linda", last_name:	"Douglas"])
		db.vets.insert([id:	4, first_name: "Rafael", last_name:	"Ortega"])
		db.vets.insert([id:	5, first_name: "Henry", last_name:	"Stevens"])
		db.vets.insert([id:	6, first_name: "Sharon", last_name:	"Jenkins"])
		assert db.vets.find().toList().size() == 6

		db.specialties.insert([id:1, name:"radiology"])
		db.specialties.insert([id:2, name:"surgery"])
		db.specialties.insert([id:3, name:"dentistry"])
		assert db.specialties.find().toList().size() == 3

		db.vet_specialties.insert([vet_id:2, specialty_id:1])
		db.vet_specialties.insert([vet_id:3, specialty_id:2])
		db.vet_specialties.insert([vet_id:3, specialty_id:3])
		db.vet_specialties.insert([vet_id:4, specialty_id:2])
		db.vet_specialties.insert([vet_id:5, specialty_id:1])
		assert db.vet_specialties.find().toList().size() == 5

		db.types.insert(id:1,name:"cat")
		db.types.insert(id:2,name:"dog")
		db.types.insert(id:3,name:"lizard")
		db.types.insert(id:4,name:"snake")
		db.types.insert(id:5,name:"bird")
		db.types.insert(id:6,name:"hamster")
		assert db.types.find().toList().size() == 6

		db.owners.insert([id:1,first_name:"George",last_name:"Franklin",
					address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"])
		db.owners.insert([id:2,first_name:"Betty",last_name:"Davis",
					address:"638 Cardinal Ave.", city:"Sun Prairie", telephone:"6085551749"])
		db.owners.insert([id:3,first_name:"Eduardo",last_name:"Rodriquez",
					address:"2693 Commerce St.", city:"McFarland", telephone:"6085558763"])
		db.owners.insert([id:4,first_name:"Harold",last_name:"Davis",
					address:"563 Friendly St.", city:"Windsor", telephone:"6085553198"])
		db.owners.insert([id:5,first_name:"Peter",last_name:"McTavish",
					address:"2387 S. Fair Way", city:"Madison", telephone:"6085552765"])
		db.owners.insert([id:6,first_name:"Jean",last_name:"Coleman",
					address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"])
		db.owners.insert([id:7,first_name:"Jeff",last_name:"Black",
					address:"1450 Oak Blvd.", city:"Monona", telephone:"6085555387"])
		db.owners.insert([id:8,first_name:"Maria",last_name:"Escobito",
					address:"345 Maple St.", city:"Madison", telephone:"6085557683"])
		db.owners.insert([id:9,first_name:"David",last_name:"Schroeder",
					address:"2749 Blackhawk Trail", city:"Madison", telephone:"6085559435"])
		db.owners.insert([id:10,first_name:"Carlos",last_name:"Estaban",
					address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"])
		assert db.owners.find().toList().size() == 10
		
		db.pets.insert([id:1, name:"Leo", 	birth_date:"2000-09-07", type_id:1, owner_id:1])
		db.pets.insert([id:2, name:"Basil", birth_date:"2002-08-06", type_id:6, owner_id:2])
		db.pets.insert([id:3, name:"Rosy", 	birth_date:"2001-04-17", type_id:2, owner_id:3])
		db.pets.insert([id:4, name:"Jewel", birth_date:"2000-03-07", type_id:2, owner_id:3])
		db.pets.insert([id:5, name:"Iggy", 	birth_date:"2000-11-30", type_id:3, owner_id:4])
		db.pets.insert([id:6, name:"George", birth_date:"2000-01-20", type_id:4, owner_id:5])
		db.pets.insert([id:7, name:"Samantha", birth_date:"1995-09-04", type_id:1, owner_id:6])
		db.pets.insert([id:8, name:"Max", 	birth_date:"1995-09-04", type_id:1, owner_id:6])
		db.pets.insert([id:9, name:"Lucky", birth_date:"1999-08-06", type_id:5, owner_id:7])
		db.pets.insert([id:10, name:"Mulligan", birth_date:"1997-02-24", type_id:2, owner_id:8])
		db.pets.insert([id:11, name:"Freddy", birth_date:"2000-03-09", type_id:5, owner_id:9])
		db.pets.insert([id:12, name:"Lucky", birth_date:"2000-06-24", type_id:2, owner_id:10])
		db.pets.insert([id:13, name:"Sly", 	birth_date:"2002-06-08", type_id:1, owner_id:10])
		assert db.pets.find().toList().size() == 13
		
		db.visits.insert ([id:1,pet_id:7,visit_date:"1996-03-04",description:"rabies shot"])
		db.visits.insert ([id:2,pet_id:8,visit_date:"1996-03-04",description:"rabies shot"])
		db.visits.insert ([id:3,pet_id:8,visit_date:"1996-06-04",description:"neutered"])
		db.visits.insert ([id:4,pet_id:7,visit_date:"1996-09-04",description:"spayed"])
		assert db.visits.find().toList().size() == 4
	}
	
	@Test
	void testUpdate () {
		db.visits.update ([visit_date:"2012-09-04"],[id:2,pet_id:8,visit_date:"1996-03-04",description:"rabies shot"])
		assert db.visits.find([visit_date:"2012-09-04"]).toList().size() == 1
	}
	
	@After
	void deleteAll () {
		db.vet_specialties.drop()
		db.vets.drop()
		db.specialties.remove ([id:1, name:"radiology"])
		db.specialties.remove ([id:2, name:"surgery"])
		db.specialties.remove ([id:3, name:"dentistry"])
		db.visits.drop()
		db.pets.drop()
		db.owners.drop()
		db.types.drop()
	}
}
