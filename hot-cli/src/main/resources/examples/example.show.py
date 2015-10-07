s = "Hello My friend"

def printHello(request):
    return s
    
def printJson(request):
	return {"items":{"name":"damien", "age":8}}
	
def onconnect(connection):
	
	def ondata():
		connection.write(data)
	
	def onclose():
		print 'close'
		
	connection.data(ondata)
	connection.close(onclose)

show.rest.get("/pyitems").headers(["Accept: text/plain"]).then(printHello)

show.rest.get("/pyitems").headers(["Accept: application/json"]).then(printJson)

show.rest.put("/pyitem").then(printHello)

show.websocket.addHandler({'path':'/pychatroom'}).connect(onconnect)