var hello = "hello world"

show.rest.get("/items").headers(["Content-Type: application/pdf"]).then(function() {
	return hello
})

show.rest.get("/items").headers(["Content-Type: application/x-www-form-urlencoded"]).then(function(request) {
	return "hello world"
})

show.rest.put("/jsitem").auth().then(function (){
	return "put"
})

show.rest.post("/jsitem").headers(["Content-Type: application/x-www-form-urlencoded","Accept: application/json"]).then(function() {
	return {items:{name:"lilou", age:8}}
})

show.websocket.addHandler({path:"/jschatroom"}).connect(function(connection) {
	connection.data(function(data) {
		connection.write(data)
	})
	connection.close(function(){ hprint("closed") })
})