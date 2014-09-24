def f1 ():
	return 1
		
def f2 ():
	res = {"body":"taratata","status":500}
	return res
		
def f3 ():
	res = {"body":"taratata","status":500, "headers":["Content Type: applocation/xml","Encoding: UTF8"]}
	return res
		
def f4 ():
	body = hot.web.pathParams["service"] + " " + hot.web.pathParams["content"]
	res = {"body":body,"status":500, "headers":["Content Type: applocation/xml","Encoding: UTF8"]}
	return res