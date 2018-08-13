import be.solidx.hot.shows.RestRequest
import hot.Response
import be.solidx.hot.promises.groovy.GroovyDeferred



show.rest.get("/scells").headers(["Accept: application/json"]).then({
	return [scells:[
		title:"baskets",
		description:'dslfjksdlfjslfjslfdjslfd'
		]
	]
})

show.rest.get("/scells/{id}").headers(["Accept: application/json"]).then({ request ->
	return [scells:[
		title:"baskets",
		description:'dslfjksdlfjslfjslfdjslfd',
		id: request.pathParams["id"]
		]
	]
})

show.rest.get("/scells").headers(["Accept: application/xml"]).then({
	return [scells:[
		title:"baskets",
		description:'dslfjksdlfjslfjslfdjslfd'
		]
	]
})

show.rest.get("/scells-slow").headers(["Accept: application/json"]).then({
	def deferred = new GroovyDeferred()
	def promise = deferred.promise()
	
	Thread.start { 
		sleep 4000
		deferred.resolve ([scells:[
			title:"baskets",
			description:'dslfjksdlfjslfjslfdjslfd'
			]
		])
	}
	return promise
})

show.rest.get("/scells-index.html").headers(["Accept: text/html"]).then({
	return '''
<html></html>
	'''
})

show.rest.get("/scells-response").headers(["Accept: application/json"]).then({
	return new Response(['Content-Type':'application/json; charset=iso8859-1'],[scells:[
		title:"baskets",
		description:'dslfjksdlfjslfjslfdjslfd'
		]
	])
})

show.rest.post('/upload').then { RestRequest req ->
    List<RestRequest.Part> parts = req.body

    def resp = parts.collect({ part ->

        def l = [name: part.name, isFile: part.file]
        if (part.isFile()) {
            def fpart = (RestRequest.FilePart)part
            l.filename = fpart.filename
            fpart.mv('/tmp/chaise.jpg')
        }
        l
    })
    new Response(['Content-Type':'application/json; charset=iso8859-1'], resp)
}