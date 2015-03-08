print("Starting...")

options = {
	'url': 'http://localhost:8080/test',
	'type': 'POST',
	'headers':{
		'Content-type':'application/xml'
	},
	'data':{
		'name':'Damien',
		'age':8
	}
}

def done(data, status, response):
    print data.getElementsByTagName("name")
    server.stop()

def fail(response, status, error):
    error.printStackTrace()
    server.stop()

client.buildRequest(options).done(done).fail(fail)
