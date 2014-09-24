var options = {
	url: 'https://localhost:8443/test',
	ca:'ca/ca.crt'
}

hprint("Starting...")

client.buildRequest(options).done(function(data) {
	hprint(data)
	server.stop()
}).fail(function(response, status, error) {
	hprint(error)
	server.stop()
})