class RestContext:
	
	def __init__(self,pathParams,principal,requestParams,requestBody):
		self.pathParams = pathParams
		self.principal = principal
		self.requestParams = requestParams
		self.requestBody = requestBody
		
restContext = RestContext(pathParams,principal,requestParams,requestBody)