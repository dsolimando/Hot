var options = {
	url: 'https://localhost:8443/test',
	ssl: {
		ca:'ca/ca.crt',
		jks:"ca/client.jks",
		jksPassword:"clientclient",
		jksCertificatePassword:"client"
	}
}

hprint("Starting...")

client.buildRequest(options).done(function(data, status, msg) {
	hprint(new java.lang.String(data))
	server.stop()
}).fail(function(error, status) {
	error.printStackTrace()
})