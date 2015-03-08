hprint("Starting...")

var options = {
	url: 'http://localhost:8080/test',
	type: 'POST',
	headers:{
		'Content-type':"application/json"
	},
	data:{
		name:'Damien',
		age:8
	}
}

client.buildRequest(options).done(function(data) {
	hprint(data.name)
	server.stop()
}).fail(function(response, status, error) {
	hprint(error)
	server.stop()
}) 