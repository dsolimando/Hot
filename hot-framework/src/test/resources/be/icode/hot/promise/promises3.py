def subThen(result):
    #raise Exception("maerde")
    return result*3

def done(result):
    print result

def then(result):
    return subdeferred.promise()

def fail(e):
    print e

def thenfail(message):
    return message + message

promise = deferred.promise()
promise.then(then,thenfail).then(subThen).done(done).fail(fail)

#subdeferred.resolve(20)
deferred.reject("Merde...")
#deferred.resolve(30)
