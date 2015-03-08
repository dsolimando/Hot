options = {
	'hostname': 'localhost',
	'port': 8443,
	'path': "/test",
	'ca':'ca/ca.crt',
	'key':"ca/client.key",
	'cert':"ca/client.crt",
	'passphrase':"client",
}

class Body:
	pass
	
def responseHandler(response):

    print "connected"
    body = Body()
    body.content = ""
	
    def dataHandler (chunck):
        body.content += chunck

    def endHandler ():
        print body.content
        print response.statusCode

    response.on("data",dataHandler)
    response.on("end", endHandler)

request = client.request (options , responseHandler)
request.end()