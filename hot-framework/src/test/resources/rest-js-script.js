function test (hot) {
	this.hot = hot;
}
test.prototype.f1 = function () {
	return 1;
}
test.prototype.f2 = function () {
	return {
		body: "taratata",
		status: 500
	}
}
test.prototype.f3 = function () {
	return {
		body: "taratata",
		status: 500,
		headers:["Content Type: applocation/xml","Encoding: UTF8"]
	}
}
test.prototype.f4 = function () {
	hprint (this.hot)
	var s = this.hot.web.pathParams.service + " " + this.hot.web.pathParams.content
	return {
		body: s,
		status: 500,
		headers:["Content Type: applocation/xml","Encoding: UTF8"]
	}
}