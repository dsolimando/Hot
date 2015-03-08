var options = {
	url: 'https://localhost:8443/test',
	ssl: {
		ca:'ca/ca.crt',
		p12:"ca/client.p12",
		passphrase:"client"
	}
}

hprint("Starting...")

client.buildRequest(options).done(function(data, status, response) {
	hprint("\n"+data[0].name)
	server.stop()
}).fail(function (response, message, error) {
	hprint(error.message)
	error.printStackTrace()
	server.stop()
})
