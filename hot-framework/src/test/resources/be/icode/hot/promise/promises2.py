def subThen(result):
    return result*3

def done(result):
    print result

def then(result):
    return subdeferred.promise()

def fail(e):
    print e

def thenfail(message):
    return message + message

def always():
    print "always"

promise = deferred.promise()
promise.then(then).then(subThen).done(done).always(always)

subdeferred.resolve(20)
deferred.resolve(10)
