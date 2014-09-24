var options = {
	url: 'https://localhost:8443/test',
	ca:'ca/ca.crt',
	p12:"ca/client.p12",
	passphrase:"client"
}

hprint("Starting...")

client.buildRequest(options).done(function(document, status, response) {
	hprint(document.childNodes.item(0).getAttribute("attr"))
	server.stop()
})