class test:
	def __init__ (self, hot):
		self.hot = hot
		
	def f1 (self):
		return 1
		
	def f2 (self):
		res = {"body":"taratata","status":500}
		return res
		
	def f3 (self):
		res = {"body":"taratata","status":500, "headers":["Content-Type: applocation/xml","Encoding: UTF8"]}
		return res
		
	def f4 (self):
		body = self.hot.web.pathParams["service"] + " " + self.hot.web.pathParams["content"]
		res = {"body":body,"status":500, "headers":["Content-Type: applocation/xml","Encoding: UTF8"]}
		return res