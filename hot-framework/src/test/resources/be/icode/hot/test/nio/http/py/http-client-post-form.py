print("Starting...")

options = {
	'url': 'http://localhost:8080/test',
	'type': 'POST',
	'headers':{
		'Content-type':'application/x-www-form-urlencoded'
	},
	'data':{
		'name':'Damien',
		'age':8
	}
}

def done(data, status, response):
    print data
    server.stop()

def fail(response, status, error):
    print "error"
    server.stop()

client.buildRequest(options).done(done).fail(fail)
