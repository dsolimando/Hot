var options = {
	hostname: 'localhost',
	port: 8080,
	path: "/rest/pendingSales",
}

var request = client.request (options , function(response) {
	
	var body = ""
	response.on ("data", function (chunck) {
		body += chunck
		hprint (chunck)
	})
	
	response.on ("end", function () {
		hprint ("end")
		//println body
	})
})

request.on("error", function (message) {
	hprint (message)
})

//Thread.currentThread().sleep(1000);
request.end()