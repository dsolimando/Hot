var options = {
	url: 'https://localhost:8443/test',
	ssl: {
		ca:'ca/ca.crt',
		key:"ca/client.key",
		cert:"ca/client.crt",
		passphrase:"client"
	}
}

hprint("Starting...")

client.buildRequest(options).done(function(data) {
	hprint(new java.lang.String(data))
	server.stop()
})