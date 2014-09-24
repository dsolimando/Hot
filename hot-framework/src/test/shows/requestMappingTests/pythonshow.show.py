s = "Hello My friend"

def printHello():
    print s

show.rest.get("/pyitems").headers(["Content-Type: application/xml"]).then(printHello)

show.rest.get("/pyitems").headers(["Content-Type: application/json"]).then(printHello)

show.rest.put("/pyitem").auth().then(printHello)

def onconnect(connection):
	
	def ondata():
		connection.write(data)
	
	def onclose():
		print 'close'
		
	connection.data(ondata)
	connection.close(onclose)

show.websocket.addHandler({'path':'/pychatroom'}).connect(onconnect)