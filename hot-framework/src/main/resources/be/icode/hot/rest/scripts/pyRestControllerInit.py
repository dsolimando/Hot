class Hot:
	def __init__(self, db, web, logger):
		self.db = db
		self.web = web
		self.logger = logger

class DBMap:
	def __init__(self,dbmap):
		self.dbmap = dbmap

	def __getattr__(self, name):
   		return self.dbmap[name]
   		
class Web:
	def __init__(self,request, GET, pathParams):
		self.request = request
		self.GET = GET
		self.pathParams = pathParams
		
try:
	hot = Hot(DBMap(__hot__db__dbmap),Web(__hot__web__request,__hot__web__get,__hot__web__pathParams),__hot__logger)
except:
	hot = Hot(DBMap(__hot__db__dbmap),Web(__hot__web__request,__hot__web__get,None),__hot__logger)