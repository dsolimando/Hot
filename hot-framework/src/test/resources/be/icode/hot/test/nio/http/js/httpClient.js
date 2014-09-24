hprint("Starting...")

client.buildRequest({
	url:"http://www.google.com/"
}).then(function(data,textStatus,response) {
	hprint("redirect->")
	return client.buildRequest({url:response.headers.Location})
}).done(function(data) {
	hprint(new java.lang.String(data))
	server.stop()
})
