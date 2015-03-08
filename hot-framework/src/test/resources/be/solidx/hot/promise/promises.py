from be.solidx.hot.promises.python import PythonDeferred

deferred = PythonDeferred()

def callback(result):
    print result["val"]

def then(result):
    return {"val":"then..."+result["val"]}

def always():
    print "always"

def fail(e):
    e.printStackTrace()

promise = deferred.promise()
promise.done(callback).always(always).then(then).done(callback).fail(fail)

deferred.resolve({"val":"hello"})