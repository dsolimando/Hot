
var promise = deferred.promise()

promise.then(function(result) {
	return subdeferred.promise();
}).then(function(result) {
	return result * 3
}).done(function(result) {
	hprint(result)
})

deferred.resolve("hello")
subdeferred.resolve(30)