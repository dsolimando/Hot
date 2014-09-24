db.getCollection("posts").drop()

var post = {"author": "Mike",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"]}

var post2 = {"author": "Julie",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"]}

var post3 = {"author": "Damien",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"],
		"doc": {
			"title":"doc1",
			"writer":"julie"
			}
		}

db.getCollection("posts").insert(post)
db.getCollection("posts").insert(post2)
db.getCollection("posts").insert(post3)

_.each (db.getCollection("posts").find().toArray(),function (obj) {
	out.println(obj["_id"]+" title: "+ obj["text"]+ ", tag: " +obj["tags"][0]);
	if (obj.doc != null) {
		out.println("val:" +obj.doc.title+"");
	}
})

out.println (db.getCollection("posts").findOne().author);
out.println (db.getCollection("posts").findOne({"author": "Mike"})._id);

out.println (db.getCollection("posts").find({"author": "Eliot"}).count());
out.println (db.getCollection("posts").find({"author": "Mike"}).count());

var criterion = {"_id": db.getCollection("posts").findOne({"author": "Mike"})._id}
out.println (db.getCollection("posts").find(criterion).count());
db.getCollection("posts").update({"author": "Toto","text":"hello world"},criterion)
db.getCollection("posts").findOne({"author": "Mike"})