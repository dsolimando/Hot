options = {
	'hostname': 'localhost',
	'port':8080,
	'path': "/test",
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