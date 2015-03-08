from com.mongodb import *
from be.solidx.hot.data.mongo import *

class DB:
	def __init__ (self,db):
		self.db = db
		
	def getCollection(self, name):
		return BasicCollection(self.db,name,DBObjectPyDictionaryTransformer())

	def __getattr__(self, name):
   		return self.getCollection(name)

mongo = Mongo()
db = DB(mongo.getDB("TestMongoTemplat"))


post = {"author": "Mike",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"]}

post2 = {"author": "Julie",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"]}
		
post3 = {"author": "Julie",
		"text": "My first blog post!",
		"tags": ["mongodb", "python", "pymongo"],
		"doc": {
			"title":"doc1",
			"writer":"julie"
			}
		}

db.posts.drop()		

db.posts.insert(post)
db.posts.insert(post2)
db.posts.insert(post3)

print db.posts.findOne()
print db.posts.findOne({"author": "Mike"})
db.posts.findOne({"author": "Eliot"})

for post in db.posts.find():
	print post

for post in db.posts.find({"author": "Mike"}):
	print post

for post in db.posts.find().sort({"author":1}):
	print post

print db.posts.find({"author": "Mike"}).count()
db.posts.update({"author":"Toto"},{"author":"Mike"})
print db.posts.find().count()
for post in db.posts.find().sort({"author":1}):
	print post