def insertData ():
	db.vets.insert({"id":	1, "first_name": "James", "last_name":	"Carter"})
	db.vets.insert({"id":	2, "first_name": "Helen", "last_name":	"Leary"})
	db.vets.insert({"id":	3, "first_name": "Linda", "last_name":	"Douglas"})
	db.vets.insert({"id":	4, "first_name": "Rafael", "last_name":	"Ortega"})
	db.vets.insert({"id":	5, "first_name": "Henry", "last_name":	"Stevens"})
	db.vets.insert({"id":	6, "first_name": "Sharon", "last_name":	"Jenkins"})

	db.specialties.insert({"id":1, "name":"radiology"})
	db.specialties.insert({"id":2, "name":"surgery"})
	db.specialties.insert({"id":3, "name":"dentistry"})

	db.vet_specialties.insert({"vet_id":2, "specialty_id":1})
	db.vet_specialties.insert({"vet_id":3, "specialty_id":2})
	db.vet_specialties.insert({"vet_id":3, "specialty_id":3})
	db.vet_specialties.insert({"vet_id":4, "specialty_id":2})
	db.vet_specialties.insert({"vet_id":5, "specialty_id":1})

	db.types.insert({"id":1,"name":"cat"})
	db.types.insert({"id":2,"name":"dog"})
	db.types.insert({"id":3,"name":"lizard"})
	db.types.insert({"id":4,"name":"snake"})
	db.types.insert({"id":5,"name":"bird"})
	db.types.insert({"id":6,"name":"hamster"})

	db.owners.insert({"id":1,"first_name":"George","last_name":"Franklin",
				"address":"110 W. Liberty St.", "city":"Madison", "telephone":"6085551023"})
	db.owners.insert({"id":2,"first_name":"Betty","last_name":"Davis",
				"address":"638 Cardinal Ave.", "city":"Sun Prairie", "telephone":"6085551749"})
	db.owners.insert({"id":3,"first_name":"Eduardo","last_name":"Rodriquez",
				"address":"2693 Commerce St.", "city":"McFarland", "telephone":"6085558763"})
	db.owners.insert({"id":4,"first_name":"Harold","last_name":"Davis",
				"address":"563 Friendly St.", "city":"Windsor", "telephone":"6085553198"})
	db.owners.insert({"id":5,"first_name":"Peter","last_name":"McTavish",
				"address":"2387 S. Fair Way", "city":"Madison", "telephone":"6085552765"})
	db.owners.insert({"id":6,"first_name":"Jean","last_name":"Coleman",
				"address":"105 N. Lake St.", "city":"Monona", "telephone":"6085552654"})
	db.owners.insert({"id":7,"first_name":"Jeff","last_name":"Black",
				"address":"1450 Oak Blvd.", "city":"Monona", "telephone":"6085555387"})
	db.owners.insert({"id":8,"first_name":"Maria","last_name":"Escobito",
				"address":"345 Maple St.", "city":"Madison", "telephone":"6085557683"})
	db.owners.insert({"id":9,"first_name":"David","last_name":"Schroeder",
				"address":"2749 Blackhawk Trail", "city":"Madison", "telephone":"6085559435"})
	db.owners.insert({"id":10,"first_name":"Carlos","last_name":"Estaban",
				"address":"2335 Independence La.", "city":"Waunakee", "telephone":"6085555487"})
	
	db.pets.insert({"id":1, "name":"Leo", 	"birth_date":"2000-09-07", "type_id":1, "owner_id":1})
	db.pets.insert({"id":2, "name":"Basil", "birth_date":"2002-08-06", "type_id":6, "owner_id":2})
	db.pets.insert({"id":3, "name":"Rosy", 	"birth_date":"2001-04-17", "type_id":2, "owner_id":3})
	db.pets.insert({"id":4, "name":"Jewel", "birth_date":"2000-03-07", "type_id":2, "owner_id":3})
	db.pets.insert({"id":5, "name":"Iggy", 	"birth_date":"2000-11-30", "type_id":3, "owner_id":4})
	db.pets.insert({"id":6, "name":"George", "birth_date":"2000-01-20", "type_id":4, "owner_id":5})
	db.pets.insert({"id":7, "name":"Samantha", "birth_date":"1995-09-04", "type_id":1, "owner_id":6})
	db.pets.insert({"id":8, "name":"Max", 	"birth_date":"1995-09-04", "type_id":1, "owner_id":6})
	db.pets.insert({"id":9, "name":"Lucky", "birth_date":"1999-08-06", "type_id":5, "owner_id":7})
	db.pets.insert({"id":10, "name":"Mulligan", "birth_date":"1997-02-24", "type_id":2, "owner_id":8})
	db.pets.insert({"id":11, "name":"Freddy", "birth_date":"2000-03-09", "type_id":5, "owner_id":9})
	db.pets.insert({"id":12, "name":"Lucky", "birth_date":"2000-06-24", "type_id":2, "owner_id":10})
	db.pets.insert({"id":13, "name":"Sly", 	"birth_date":"2002-06-08", "type_id":1, "owner_id":10})
	
	db.visits.insert ({"id":1,"pet_id":7,"visit_date":"1996-03-04","description":"rabies shot"})
	db.visits.insert ({"id":2,"pet_id":8,"visit_date":"1996-03-04","description":"rabies shot"})
	db.visits.insert ({"id":3,"pet_id":8,"visit_date":"1996-06-04","description":"neutered"})
	db.visits.insert ({"id":4,"pet_id":7,"visit_date":"1996-09-04","description":"spayed"})

insertData()
db.visits.update ({"visit_date":"2012-09-04"},{"id":2,"pet_id":8,"visit_date":"1996-03-04","description":"rabies shot"})

def testFindWhere1():
	l = list(db.owners.find({"id":6}))
	assert len(l) == 1
	assert l[0] == {"id":6, "first_name":"Jean", "last_name":"Coleman", "address":"105 N. Lake St.", "city":"Monona", "telephone":"6085552654"}

def testFindWhere2():
	l = list(db.owners.find({"id":6,"first_name":"Jean"}))
	assert len(l) == 1
	assert l[0] == {"id":6, "first_name":"Jean", "last_name":"Coleman", "address":"105 N. Lake St.", "city":"Monona", "telephone":"6085552654"}

def testFindWhere3():
	l = list(db.owners.find({"id":6,"first_name":"Carlos"}))
	assert len(l) == 0

def testFindWhere4():
	l = list(db.owners.find({"$or":[{"first_name":"Carlos"},{"first_name":"George"}]}))
	assert len(l) == 2
	assert l[0] == {"id":1, "first_name":"George", "last_name":"Franklin", "address":"110 W. Liberty St.", "city":"Madison", "telephone":"6085551023"}
	assert l[1] == {"id":10, "first_name":"Carlos", "last_name":"Estaban", "address":"2335 Independence La.", "city":"Waunakee", "telephone":"6085555487"}

def testFindWhere5():
	l = list(db.owners.find({"$or":[{"first_name":"Carlos"},{"first_name":"George"}],"city":"Madison"}))
	assert len(l) == 1
	assert l[0] == {"id":1, "first_name":"George", "last_name":"Franklin", "address":"110 W. Liberty St.", "city":"Madison", "telephone":"6085551023"}

def testFindWhere6():
	l = list(db.owners.find({"$or":[{"first_name":{"$like":"Carl%"}},{"first_name":{"$like":"%orge"}},{"first_name":{"$like":"%ett%"}}]}))
	assert len(l) == 3
	assert l[0] == {u'last_name': u'Franklin', u'first_name': u'George', u'city': u'Madison', u'telephone': u'6085551023', u'id': 1, u'address': u'110 W. Liberty St.'}
	assert l[1] == {u'last_name': u'Davis', u'first_name': u'Betty', u'city': u'Sun Prairie', u'telephone': u'6085551749', u'id': 2, u'address': u'638 Cardinal Ave.'}
	assert l[2] == {u'last_name': u'Estaban', u'first_name': u'Carlos', u'city': u'Waunakee', u'telephone': u'6085555487', u'id': 10, u'address': u'2335 Independence La.'}
	
def testFindWhere7():
	l = list(db.owners.find({"$or":[{"id":{"$mod":[2, 0]}},{"id":{"$mod":[5, 0]}}]}))
	assert len(l) == 6
	assert l[0] == {u'last_name': u'Davis', u'first_name': u'Betty', u'city': u'Sun Prairie', u'telephone': u'6085551749', u'id': 2, u'address': u'638 Cardinal Ave.'}
	assert l[1] == {u'last_name': u'Davis', u'first_name': u'Harold', u'city': u'Windsor', u'telephone': u'6085553198', u'id': 4, u'address': u'563 Friendly St.'}
	assert l[2] == {u'last_name': u'McTavish', u'first_name': u'Peter', u'city': u'Madison', u'telephone': u'6085552765', u'id': 5, u'address': u'2387 S. Fair Way'}
	assert l[3] == {u'last_name': u'Coleman', u'first_name': u'Jean', u'city': u'Monona', u'telephone': u'6085552654', u'id': 6, u'address': u'105 N. Lake St.'}
	assert l[4] == {u'last_name': u'Escobito', u'first_name': u'Maria', u'city': u'Madison', u'telephone': u'6085557683', u'id': 8, u'address': u'345 Maple St.'}
	assert l[5] == {u'last_name': u'Estaban', u'first_name': u'Carlos', u'city': u'Waunakee', u'telephone': u'6085555487', u'id': 10, u'address': u'2335 Independence La.'}

def testFindWhere8():
	l = list(db.owners.find({"first_name":{"$in":["George", "Jean", "Carlos"]}}))
	assert len(l) == 3
	assert l[0] == {u'last_name': u'Franklin', u'first_name': u'George', u'city': u'Madison', u'telephone': u'6085551023', u'id': 1, u'address': u'110 W. Liberty St.'}
	assert l[1] == {u'last_name': u'Coleman', u'first_name': u'Jean', u'city': u'Monona', u'telephone': u'6085552654', u'id': 6, u'address': u'105 N. Lake St.'}
	assert l[2] == {u'last_name': u'Estaban', u'first_name': u'Carlos', u'city': u'Waunakee', u'telephone': u'6085555487', u'id': 10, u'address': u'2335 Independence La.'}
		
def testFindWhere9():
	l = list(db.owners.find({"first_name":{"$nin":["George", "Jean", "Carlos"]}}))
	assert len(l) == 7
	assert l[0] == {u'last_name': u'Davis', u'first_name': u'Betty', u'city': u'Sun Prairie', u'telephone': u'6085551749', u'id': 2, u'address': u'638 Cardinal Ave.'}
	assert l[1] == {u'last_name': u'Rodriquez', u'first_name': u'Eduardo', u'city': u'McFarland', u'telephone': u'6085558763', u'id': 3, u'address': u'2693 Commerce St.'}
	assert l[2] == {u'last_name': u'Davis', u'first_name': u'Harold', u'city': u'Windsor', u'telephone': u'6085553198', u'id': 4, u'address': u'563 Friendly St.'}
	assert l[3] == {u'last_name': u'McTavish', u'first_name': u'Peter', u'city': u'Madison', u'telephone': u'6085552765', u'id': 5, u'address': u'2387 S. Fair Way'}
	assert l[4] == {u'last_name': u'Black', u'first_name': u'Jeff', u'city': u'Monona', u'telephone': u'6085555387', u'id': 7, u'address': u'1450 Oak Blvd.'}
	assert l[5] == {u'last_name': u'Escobito', u'first_name': u'Maria', u'city': u'Madison', u'telephone': u'6085557683', u'id': 8, u'address': u'345 Maple St.'}
	assert l[6] == {u'last_name': u'Schroeder', u'first_name': u'David', u'city': u'Madison', u'telephone': u'6085559435', u'id': 9, u'address': u'2749 Blackhawk Trail'}

def testFindWhere10():
	l = list(db.owners.join(["pets", "pets.types"]).find({"pets.name":"Leo","types.name":"cat"}))
	assert len(l) == 1
	assert l[0]["id"] == 1
	assert l[0]["first_name"] == "George"
	assert l[0]["address"] == "110 W. Liberty St."
	assert l[0]["last_name"] == "Franklin"
	assert l[0]["telephone"] == "6085551023"
	assert l[0]["city"] == "Madison"
	assert len(l[0]["pets"]) == 1
	assert l[0]["pets"][0]["id"] == 1
	assert l[0]["pets"][0]["name"] == "Leo"
	assert l[0]["pets"][0]["type"] != None
	assert l[0]["pets"][0]["type"]["id"] == 1
	assert l[0]["pets"][0]["type"]["name"] == "cat"
	
testFindWhere1()	
testFindWhere2()	
testFindWhere3()	
testFindWhere4()
testFindWhere5()
testFindWhere6()
testFindWhere7()
testFindWhere8()
testFindWhere9()
testFindWhere10()
