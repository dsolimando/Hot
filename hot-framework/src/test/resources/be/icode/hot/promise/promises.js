
var promise = deferred.promise()

promise.done(function(result) {
	hprint(result)
}).fail(function(result) {
	hprint(result)
}).always(function() {
	hprint ("always")
})

deferred.resolve("hello")