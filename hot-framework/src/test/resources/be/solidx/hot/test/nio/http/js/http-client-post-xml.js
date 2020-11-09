hprint("Starting...")

var options = {
	url: 'http://localhost:8080/test',
	type: 'POST',
	headers:{
		'Content-type':"application/xml"
	},
	data:{
		name:'Damien',
		age:8
	}
}

client.buildRequest(options).done(function(document) {
	//hprint(document.getDocumentElement())
	server.stop()
}).fail(function(response, status, error) {
	//error.printStackTrace()
	server.stop()
}) 