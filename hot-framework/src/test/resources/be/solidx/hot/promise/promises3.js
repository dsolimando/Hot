
var promise = deferred.promise()

promise.then(function(result) {
	return subdeferred.promise();
}).then(function(result) {
	return result * 3
},function(value) {
	return value + value;
}).done(function(result) {
	hprint(result)
}).fail(function(message) {
	hprint(message)
})

deferred.reject("Oops")
subdeferred.resolve(30)