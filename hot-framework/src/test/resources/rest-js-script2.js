var f1 = function () {
	return 1;
}
var f2 = function () {
	return {
		body: "taratata",
		status: 500
	}
}
var f3 = function () {
	return {
		body: "taratata",
		status: 500,
		headers:["Content Type: applocation/xml","Encoding: UTF8"]
	}
}
var f4 = function () {
	var s = this.hot.web.pathParams.service + " " + this.hot.web.pathParams.content
	return {
		body: s,
		status: 500,
		headers:["Content Type: applocation/xml","Encoding: UTF8"]
	}
}