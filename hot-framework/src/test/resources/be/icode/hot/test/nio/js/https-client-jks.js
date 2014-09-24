var options = {
	hostname: 'localhost',
	port: 8443,
	path: '/test',
	ca:'ca/ca.crt',
	headers:{
		//tomcat:tomcat
		Authorization: "Basic dG9tY2F0OnRvbWNhdA=="
	}
}

var request = client.request(options, function(response) {
	var body = ""
	response.on('data', function(chunck) {
		body+=chunck
	})
	
	response.on('end', function() {
		hprint(body)
		hprint(response.statusCode)
	})
})

request.on('error', function(message) {
	hprint(message)
})

request.end()