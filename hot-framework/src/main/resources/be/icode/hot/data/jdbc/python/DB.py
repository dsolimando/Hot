class DB ():
	def __init__ (self, db):
		self.db = db
		
	def getCollection(self, name):
		return self.db.getCollection(name)

	def __getattr__(self, name):
   		return self.getCollection(name)