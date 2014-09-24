var options = {
	url: 'https://localhost:8443/test',
	rejectUnauthorized:false
}

hprint("Starting...")

client.buildRequest(options).done(function(data) {
	hprint(new java.lang.String(data))
	server.stop()
})