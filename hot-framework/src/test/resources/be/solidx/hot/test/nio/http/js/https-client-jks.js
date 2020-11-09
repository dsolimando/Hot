var options = {
	url: 'https://localhost:8443/test',
	ssl: {
		ca:'ca/ca.crt'
	}
}

hprint("Starting...")

client.buildRequest(options).done(function(data, status, response) {
	hprint(status)
	server.stop()
}).fail(function(error, status) {
	error.printStackTrace()
	server.stop()
})