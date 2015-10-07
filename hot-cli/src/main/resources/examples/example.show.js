var hello = "var get method"

show.rest.get("/jsitems").headers(["Accept: text/plain"]).then(function() {
	return hello
})

show.rest.get("/jsitems").headers(["Accept: application/json"]).then(function(request) {
	return {items:{name:"damien", age:8}}
})

show.rest.put("/jsitem").then(function (){
	return "put method"
})

show.websocket.addHandler({path:"/jschatroom"}).connect(function(connection) {
	connection.data(function(data) {
		connection.write(data)
	})
	connection.close(function(){ hprint("closed") })
})