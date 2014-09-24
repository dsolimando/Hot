#db = DB(bdb)

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
	promise = adb.owners.find({"id":6}).promise()

	def done (l):
		assert len(l) == 1
		assert l[0] == {"id":6, "first_name":"Jean", "last_name":"Coleman", "address":"105 N. Lake St.", "city":"Monona", "telephone":"6085552654"}
		print "done"

	promise.done(done)


def testFindWhere2():
	promise = adb.owners.find({"id":6,"first_name":"Jean"}).promise()
	
	def done (l):	
		assert len(l) == 1
		assert l[0] == {"id":6, "first_name":"Jean", "last_name":"Coleman", "address":"105 N. Lake St.", "city":"Monona", "telephone":"6085552654"}
		
	promise.done(done)

def testFindWhere3():
	promise = adb.owners.find({"id":6,"first_name":"Carlos"}).promise()
	
	def done(l):
		assert len(l) == 0
		
	promise.done(done)

def testFindWhere4():
	promise = adb.owners.find({"$or":[{"first_name":"Carlos"},{"first_name":"George"}]}).promise()
	
	def done(l):
		assert len(l) == 2
		assert l[0] == {"id":1, "first_name":"George", "last_name":"Franklin", "address":"110 W. Liberty St.", "city":"Madison", "telephone":"6085551023"}
		assert l[1] == {"id":10, "first_name":"Carlos", "last_name":"Estaban", "address":"2335 Independence La.", "city":"Waunakee", "telephone":"6085555487"}
		
	promise.done(done)

def testFindWhere5():
	promise = adb.owners.find({"$or":[{"first_name":"Carlos"},{"first_name":"George"}],"city":"Madison"}).promise()
	
	def done(l):
		assert len(l) == 1
		assert l[0] == {"id":1, "first_name":"George", "last_name":"Franklin", "address":"110 W. Liberty St.", "city":"Madison", "telephone":"6085551023"}
		
	promise.done(done)

def testFindWhere6():
	promise = adb.owners.find({"$or":[{"first_name":{"$like":"Carl%"}},{"first_name":{"$like":"%orge"}},{"first_name":{"$like":"%ett%"}}]}).promise()
	
	def done(l):
		assert len(l) == 3
		assert l[0] == {'last_name': 'Franklin', 'first_name': 'George', 'city': 'Madison', 'telephone': '6085551023', 'id': 1, 'address': '110 W. Liberty St.'}
		assert l[1] == {'last_name': 'Davis', 'first_name': 'Betty', 'city': 'Sun Prairie', 'telephone': '6085551749', 'id': 2, 'address': '638 Cardinal Ave.'}
		assert l[2] == {'last_name': 'Estaban', 'first_name': 'Carlos', 'city': 'Waunakee', 'telephone': '6085555487', 'id': 10, 'address': '2335 Independence La.'}
		
	promise.done(done)
	
def testFindWhere7():
	promise = adb.owners.find({"$or":[{"id":{"$mod":[2, 0]}},{"id":{"$mod":[5, 0]}}]}).promise()
	
	def done(l):
		assert len(l) == 6
		assert l[0] == {'last_name': 'Davis', 'first_name': 'Betty', 'city': 'Sun Prairie', 'telephone': '6085551749', 'id': 2, 'address': '638 Cardinal Ave.'}
		assert l[1] == {'last_name': 'Davis', 'first_name': 'Harold', 'city': 'Windsor', 'telephone': '6085553198', 'id': 4, 'address': '563 Friendly St.'}
		assert l[2] == {'last_name': 'McTavish', 'first_name': 'Peter', 'city': 'Madison', 'telephone': '6085552765', 'id': 5, 'address': '2387 S. Fair Way'}
		assert l[3] == {'last_name': 'Coleman', 'first_name': 'Jean', 'city': 'Monona', 'telephone': '6085552654', 'id': 6, 'address': '105 N. Lake St.'}
		assert l[4] == {'last_name': 'Escobito', 'first_name': 'Maria', 'city': 'Madison', 'telephone': '6085557683', 'id': 8, 'address': '345 Maple St.'}
		assert l[5] == {'last_name': 'Estaban', 'first_name': 'Carlos', 'city': 'Waunakee', 'telephone': '6085555487', 'id': 10, 'address': '2335 Independence La.'}
		print 'succeed'
		
	promise.done(done)

def testFindWhere8():
	promise = adb.owners.find({"first_name":{"$in":["George", "Jean", "Carlos"]}}).promise()
	
	def done(l):
		assert len(l) == 3
		assert l[0] == {'last_name': 'Franklin', 'first_name': 'George', 'city': 'Madison', 'telephone': '6085551023', 'id': 1, 'address': '110 W. Liberty St.'}
		assert l[1] == {'last_name': 'Coleman', 'first_name': 'Jean', 'city': 'Monona', 'telephone': '6085552654', 'id': 6, 'address': '105 N. Lake St.'}
		assert l[2] == {'last_name': 'Estaban', 'first_name': 'Carlos', 'city': 'Waunakee', 'telephone': '6085555487', 'id': 10, 'address': '2335 Independence La.'}
		print "succeed"
		
	promise.done(done)
		
def testFindWhere9():
	promise = adb.owners.find({"first_name":{"$nin":["George", "Jean", "Carlos"]}}).promise()
	
	def done(l):
		assert len(l) == 7
		assert l[0] == {'last_name': 'Davis', 'first_name': 'Betty', 'city': 'Sun Prairie', 'telephone': '6085551749', 'id': 2, 'address': '638 Cardinal Ave.'}
		assert l[1] == {'last_name': 'Rodriquez', 'first_name': 'Eduardo', 'city': 'McFarland', 'telephone': '6085558763', 'id': 3, 'address': '2693 Commerce St.'}
		assert l[2] == {'last_name': 'Davis', 'first_name': 'Harold', 'city': 'Windsor', 'telephone': '6085553198', 'id': 4, 'address': '563 Friendly St.'}
		assert l[3] == {'last_name': 'McTavish', 'first_name': 'Peter', 'city': 'Madison', 'telephone': '6085552765', 'id': 5, 'address': '2387 S. Fair Way'}
		assert l[4] == {'last_name': 'Black', 'first_name': 'Jeff', 'city': 'Monona', 'telephone': '6085555387', 'id': 7, 'address': '1450 Oak Blvd.'}
		assert l[5] == {'last_name': 'Escobito', 'first_name': 'Maria', 'city': 'Madison', 'telephone': '6085557683', 'id': 8, 'address': '345 Maple St.'}
		assert l[6] == {'last_name': 'Schroeder', 'first_name': 'David', 'city': 'Madison', 'telephone': '6085559435', 'id': 9, 'address': '2749 Blackhawk Trail'}
		print "succeed"
		
	promise.done(done)

def testFindWhere10():
	promise = adb.owners.join(["pets", "pets.types"]).find({"pets.name":"Leo","types.name":"cat"}).promise()
	
	def done(l):
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
		print "succeed"
	
	promise.done(done)
	
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
