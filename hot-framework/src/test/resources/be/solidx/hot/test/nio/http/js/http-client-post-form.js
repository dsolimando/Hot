hprint("Starting...")

var options = {
	url: 'http://localhost:8080/test',
	type: 'POST',
	headers:{
		'Content-type':"application/x-www-form-urlencoded"
	},
	data:{
		name:'Damien',
		age:8
	}
}

client.buildRequest(options).done(function(data) {
	hprint(new java.lang.String(data))
	server.stop()
}).fail(function(response, status, error) {
	hprint(error)
	server.stop()
}) 