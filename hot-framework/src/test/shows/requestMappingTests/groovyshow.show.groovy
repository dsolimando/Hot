def s = "hello world in my eyes"

rest.get("/items").headers(["Content-Type: application/xml"]).then({
	return "toto"
})

websocket.addHandler([path:"/chatroom"]).connect({ connection ->
	connection.data { data ->
		connection.write data
	}
	connection.close { print "closed" }
})

show.rest.get("/items").headers(["Content-Type: application/json"]).then({
	[items:[name:"damien", age:8]]
})

show.rest.put("/item").auth().then({
	print "put"
})
