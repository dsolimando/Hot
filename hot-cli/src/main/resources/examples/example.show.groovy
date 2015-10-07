def s = "hello world in my eyes"

rest.get("/items").headers(["Accept: text/plein"]).then({
	return s
})

websocket.addHandler([path:"/chatroom"]).connect({ connection ->
	connection.data { data ->
		connection.write data
	}
	connection.close { print "closed" }
})

show.rest.get("/items").headers(["Accept: application/json"]).then({
	[items:[name:"damien", age:8]]
})

show.rest.put("/item").then({
	print "put"
})
