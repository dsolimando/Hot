function insert1 () {
	var vers = db.getCollection("vets").find().toArray();
	db.getCollection("vets").insert({id:	1, first_name: "James", last_name:	"Carter"})
	db.getCollection("vets").insert({id:	2, first_name: "Helen", last_name:	"Leary"})
	db.getCollection("vets").insert({id:	3, first_name: "Linda", last_name:	"Douglas"})
	db.getCollection("vets").insert({id:	4, first_name: "Rafael", last_name:	"Ortega"})
	db.getCollection("vets").insert({id:	5, first_name: "Henry", last_name:	"Stevens"})
	db.getCollection("vets").insert({id:	6, first_name: "Sharon", last_name:	"Jenkins"})

	db.getCollection("specialties").insert({id:1, name:"radiology"})
	db.getCollection("specialties").insert({id:2, name:"surgery"})
	db.getCollection("specialties").insert({id:3, name:"dentistry"})

	db.getCollection("vet_specialties").insert({vet_id:2, specialty_id:1})
	db.getCollection("vet_specialties").insert({vet_id:3, specialty_id:2})
	db.getCollection("vet_specialties").insert({vet_id:3, specialty_id:3})
	db.getCollection("vet_specialties").insert({vet_id:4, specialty_id:2})
	db.getCollection("vet_specialties").insert({vet_id:5, specialty_id:1})

	db.getCollection("types").insert({id:1,name:"cat"})
	db.getCollection("types").insert({id:2,name:"dog"})
	db.getCollection("types").insert({id:3,name:"lizard"})
	db.getCollection("types").insert({id:4,name:"snake"})
	db.getCollection("types").insert({id:5,name:"bird"})
	db.getCollection("types").insert({id:6,name:"hamster"})

	db.getCollection("owners").insert({id:1,first_name:"George",last_name:"Franklin",
				address:"110 W. Liberty St.", city:"Madison", telephone:"6085551023"})
	db.getCollection("owners").insert({id:2,first_name:"Betty",last_name:"Davis",
				address:"638 Cardinal Ave.", city:"Sun Prairie", telephone:"6085551749"})
	db.getCollection("owners").insert({id:3,first_name:"Eduardo",last_name:"Rodriquez",
				address:"2693 Commerce St.", city:"McFarland", telephone:"6085558763"})
	db.getCollection("owners").insert({id:4,first_name:"Harold",last_name:"Davis",
				address:"563 Friendly St.", city:"Windsor", telephone:"6085553198"})
	db.getCollection("owners").insert({id:5,first_name:"Peter",last_name:"McTavish",
				address:"2387 S. Fair Way", city:"Madison", telephone:"6085552765"})
	db.getCollection("owners").insert({id:6,first_name:"Jean",last_name:"Coleman",
				address:"105 N. Lake St.", city:"Monona", telephone:"6085552654"})
	db.getCollection("owners").insert({id:7,first_name:"Jeff",last_name:"Black",
				address:"1450 Oak Blvd.", city:"Monona", telephone:"6085555387"})
	db.getCollection("owners").insert({id:8,first_name:"Maria",last_name:"Escobito",
				address:"345 Maple St.", city:"Madison", telephone:"6085557683"})
	db.getCollection("owners").insert({id:9,first_name:"David",last_name:"Schroeder",
				address:"2749 Blackhawk Trail", city:"Madison", telephone:"6085559435"})
	db.getCollection("owners").insert({id:10,first_name:"Carlos",last_name:"Estaban",
				address:"2335 Independence La.", city:"Waunakee", telephone:"6085555487"})
	
	db.getCollection("pets").insert({id:1, name:"Leo", 	birth_date:"2000-09-07", type_id:1, owner_id:1})
	db.getCollection("pets").insert({id:2, name:"Basil", birth_date:"2002-08-06", type_id:6, owner_id:2})
	db.getCollection("pets").insert({id:3, name:"Rosy", 	birth_date:"2001-04-17", type_id:2, owner_id:3})
	db.getCollection("pets").insert({id:4, name:"Jewel", birth_date:"2000-03-07", type_id:2, owner_id:3})
	db.getCollection("pets").insert({id:5, name:"Iggy", 	birth_date:"2000-11-30", type_id:3, owner_id:4})
	db.getCollection("pets").insert({id:6, name:"George", birth_date:"2000-01-20", type_id:4, owner_id:5})
	db.getCollection("pets").insert({id:7, name:"Samantha", birth_date:"1995-09-04", type_id:1, owner_id:6})
	db.getCollection("pets").insert({id:8, name:"Max", 	birth_date:"1995-09-04", type_id:1, owner_id:6})
	db.getCollection("pets").insert({id:9, name:"Lucky", birth_date:"1999-08-06", type_id:5, owner_id:7})
	db.getCollection("pets").insert({id:10, name:"Mulligan", birth_date:"1997-02-24", type_id:2, owner_id:8})
	db.getCollection("pets").insert({id:11, name:"Freddy", birth_date:"2000-03-09", type_id:5, owner_id:9})
	db.getCollection("pets").insert({id:12, name:"Lucky", birth_date:"2000-06-24", type_id:2, owner_id:10})
	db.getCollection("pets").insert({id:13, name:"Sly", 	birth_date:"2002-06-08", type_id:1, owner_id:10})
	
	db.getCollection("visits").insert ({id:1,pet_id:7,visit_date:"1996-03-04",description:"rabies shot"})
	db.getCollection("visits").insert ({id:2,pet_id:8,visit_date:"1996-03-04",description:"rabies shot"})
	db.getCollection("visits").insert ({id:3,pet_id:8,visit_date:"1996-06-04",description:"neutered"})
	db.getCollection("visits").insert ({id:4,pet_id:7,visit_date:"1996-09-04",description:"spayed"})
}

insert1()
db.getCollection("visits").update ({visit_date:"2012-09-04"},{id:2,pet_id:8,visit_date:"1996-03-04",description:"rabies shot"})

QUnit.init();
QUnit.config.blocking = false;
QUnit.config.autorun = true;
QUnit.config.updateRate = 0;
QUnit.log = function(result) {
	hprint (result.name +" => "+result.message)
    hprintln(result.result ? ': PASS': ': FAIL');
};

var $qt = QUnit.test

$qt("testFindWhere1", function(assert) {
	var l = db.getCollection("owners").find({id:6}).toArray()
	assert.ok (l.length == 1, "Number of results")
	assert.deepEqual (l[0], {
			id:6, 
			first_name:"Jean", 
			last_name:"Coleman", 
			address:"105 N. Lake St.", 
			city:"Monona", 
			telephone:"6085552654"
		},"result equality")
})

$qt("testFindWhere2", function(assert) {
	var l = db.getCollection("owners").find({id:6,first_name:"Jean"}).toArray()
	assert.ok (l.length == 1, "Number of results")
	assert.deepEqual (l[0], {
			id:6, 
			first_name:"Jean", 
			last_name:"Coleman", 
			address:"105 N. Lake St.", 
			city:"Monona", 
			telephone:"6085552654"
		},"result equality")
})

$qt("testFindWhere3", function(assert) {
	var l = db.getCollection("owners").find({id:6,first_name:"Carlos"}).toArray()
	assert.ok (l.length == 0, "Number of results")
})

$qt("testFindWhere4", function(assert) {
	var l = db.getCollection("owners").find({$or:[
	                                  				{first_name:"Carlos"},
	                                				{first_name:"George"}
												]}).toArray()
	assert.ok (l.length == 2, "Number of results")
	assert.deepEqual (l[0], {
							"id":1, 
							"first_name":"George", 
							"last_name":"Franklin", 
							"address":"110 W. Liberty St.", 
							"city":"Madison", 
							"telephone":"6085551023"})
	assert.deepEqual (l[1], {
							"id":10, 
							"first_name":"Carlos", 
							"last_name":"Estaban", 
							"address":"2335 Independence La.", 
							"city":"Waunakee", 
							"telephone":"6085555487"})
})

$qt("testFindWhere5", function(assert) {
	var l = db.getCollection("owners").find({$or:[
		                                             {first_name:"Carlos"},
		                                             {first_name:"George"}
		                                        ],city:"Madison"}).toArray()
    assert.ok (l.length == 1, "Number of results")
	assert.deepEqual (l[0], {
							"id":1, 
							"first_name":"George", 
							"last_name":"Franklin", 
							"address":"110 W. Liberty St.", 
							"city":"Madison", 
							"telephone":"6085551023"})	                                        
})

$qt("testFindWhere6", function(assert) {
	var l = db.getCollection("owners").find({$or:[
	                                  				{first_name:{$like:"Carl%"}},
	                                				{first_name:{$like:"%orge"}},
	                                				{first_name:{$like:"%ett%"}}
	                                			]}).toArray()
    assert.ok (l.length == 3, "Number of results")
    
    assert.deepEqual (l[0], {
		"id":1, 
		"first_name":"George", 
		"last_name":"Franklin", 
		"address":"110 W. Liberty St.", 
		"city":"Madison", 
		"telephone":"6085551023"});
	
	assert.deepEqual (l[1], {
		'last_name': 'Davis', 
		'first_name': 'Betty', 
		'city': 'Sun Prairie', 
		'telephone': '6085551749', 
		'id': 2, 
		'address': '638 Cardinal Ave.'});	  
	
	assert.deepEqual (l[2], {
		'last_name': 'Estaban', 
		'first_name': 'Carlos', 
		'city': 'Waunakee', 
		'telephone': '6085555487', 
		'id': 10, 
		'address': '2335 Independence La.'});
})

$qt("testFindWhere7", function(assert) {
	var l = db.getCollection("owners").find({$or:[
	                                  				{id:{$mod:[2, 0]}},
	                                				{id:{$mod:[5, 0]}}
	                                			]}).toArray()
    assert.ok (l.length == 6, "Number of results")
    
    assert.deepEqual (l[0], {
		'last_name': 'Davis', 
		'first_name': 'Betty', 
		'city': 'Sun Prairie', 
		'telephone': '6085551749', 
		'id': 2, 
		'address': '638 Cardinal Ave.'});
	
	assert.deepEqual (l[1], {
		'last_name': 'Davis', 
		'first_name': 'Harold', 
		'city': 'Windsor', 
		'telephone': '6085553198', 
		'id': 4, 
		'address': '563 Friendly St.'});
	
	assert.deepEqual (l[2], {
		'last_name': 'McTavish', 
		'first_name': 'Peter',
		'city': 'Madison', 
		'telephone': '6085552765', 
		'id': 5, 
		'address': 
		'2387 S. Fair Way'});
	
	assert.deepEqual (l[3], {
		'last_name': 'Coleman', 
		'first_name': 'Jean', 
		'city': 'Monona', 
		'telephone': '6085552654', 
		'id': 6, 'address': 
		'105 N. Lake St.'}
	);
	
	assert.deepEqual (l[4], {
		'last_name': 'Escobito', 
		'first_name': 'Maria', 
		'city': 'Madison', 
		'telephone': '6085557683', 
		'id': 8, 
		'address': '345 Maple St.'}
	);
	
	assert.deepEqual (l[5], {
		'last_name': 'Estaban', 
		'first_name': 'Carlos', 
		'city': 'Waunakee', 
		'telephone': '6085555487', 
		'id': 10, 
		'address': '2335 Independence La.'}
	);
})

$qt("testFindWhere8", function(assert) {
	var l = db.getCollection("owners").find({first_name:{$in:["George", "Jean", "Carlos"]}}).toArray()
    
	assert.ok (l.length == 3, "Number of results")
	
	assert.deepEqual (l[0], {
		'last_name': 'Franklin', 
		'first_name': 'George', 
		'city': 'Madison', 
		'telephone': '6085551023', 
		'id': 1, 
		'address': '110 W. Liberty St.'}
	);
		
	assert.deepEqual (l[1], {
		'last_name': 'Coleman', 
		'first_name': 'Jean', 
		'city': 'Monona', 
		'telephone': '6085552654', 
		'id': 6, 'address': 
		'105 N. Lake St.'}
	);
	
	assert.deepEqual (l[2], {
		'last_name': 'Estaban', 
		'first_name': 'Carlos', 
		'city': 'Waunakee', 
		'telephone': '6085555487', 
		'id': 10, 
		'address': '2335 Independence La.'}
	);
})

$qt("testFindWhere9", function(assert) {
	var l = db.getCollection("owners").find({first_name:{$nin:["George", "Jean", "Carlos"]}}).toArray()
    
	assert.ok (l.length == 7, "Number of results")
	
	assert.deepEqual (l[0],  {
		'last_name': 'Davis', 
		'first_name': 'Betty', 
		'city': 'Sun Prairie', 
		'telephone': '6085551749', 
		'id': 2, 
		'address': '638 Cardinal Ave.'}
	);
		
	assert.deepEqual (l[1], {
		'last_name': 'Rodriquez', 
		'first_name': 'Eduardo', 
		'city': 'McFarland', 
		'telephone': '6085558763', 
		'id': 3, 
		'address': '2693 Commerce St.'}
	);
	
	assert.deepEqual (l[2], {
		'last_name': 'Davis', 
		'first_name': 'Harold', 
		'city': 'Windsor', 
		'telephone': '6085553198', 
		'id': 4, 
		'address': '563 Friendly St.'}
	);
	
	
	assert.deepEqual (l[3], {
		'last_name': 'McTavish', 
		'first_name': 'Peter', 
		'city': 'Madison', 
		'telephone': '6085552765', 
		'id': 5, 
		'address': '2387 S. Fair Way'}
	);
	
	assert.deepEqual (l[4], {
		'last_name': 'Black', 
		'first_name': 'Jeff', 
		'city': 'Monona', 
		'telephone': '6085555387', 
		'id': 7, 
		'address': '1450 Oak Blvd.'
	});
	
	assert.deepEqual (l[5], {
		'last_name': 'Escobito', 
		'first_name': 'Maria', 
		'city': 'Madison', 
		'telephone': '6085557683', 
		'id': 8, 
		'address': '345 Maple St.'}
	);
	
	assert.deepEqual (l[6], {
		'last_name': 'Schroeder', 
		'first_name': 'David', 
		'city': 'Madison', 
		'telephone': '6085559435', 
		'id': 9, 
		'address': '2749 Blackhawk Trail'}
	);
})
  
$qt("testFindWhere10", function(assert) {
	var l = db.getCollection("owners")
				.join(["pets", "pets.types"])
				.find({"pets.name":"Leo","types.name":"cat"}).toArray()

	assert.ok (l.length == 1, "Number of results")
	
	assert.ok( l[0]["id"] == 1)
	assert.ok( l[0]["first_name"] == "George")
	assert.ok( l[0]["address"] == "110 W. Liberty St.")
	assert.ok( l[0]["last_name"] == "Franklin")
	assert.ok( l[0]["telephone"] == "6085551023")
	assert.ok( l[0]["city"] == "Madison")
	assert.ok( l[0]["pets"].length == 1)
	assert.ok( l[0]["pets"][0]["id"] == 1)
	assert.ok( l[0]["pets"][0]["name"] == "Leo")
	assert.ok( l[0]["pets"][0]["type"])
	assert.ok( l[0]["pets"][0]["type"]["id"] == 1)
	assert.ok( l[0]["pets"][0]["type"]["name"] == "cat")
})