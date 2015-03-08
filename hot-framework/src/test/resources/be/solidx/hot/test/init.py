class Hot:
	pass

class DBMap:
	def __init__(self,dbmap):
		self.dbmap = dbmap

	def __getattr__(self, name):
   		return self.dbmap[name]