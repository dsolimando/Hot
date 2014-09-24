var test = (function (hot) {
	var hot = hot
	
	return {
		f1: function () {
			return 1;
		},
		f2: function () {
			return {
				body: "taratata",
				status: 500
			}
		},
		f3: function () {
			return {
				body: "taratata",
				status: 500,
				headers:["Content Type: applocation/xml","Encoding: UTF8"]
			}
		},
		f4: function() {
			var s = hot.web.pathParams.service + " " + hot.web.pathParams.content
			return {
				body: s,
				status: 500,
				headers:["Content Type: applocation/xml","Encoding: UTF8"]
			}
		},
		f5: function() {
			var s = hot.web.pathParams.service + " " + hot.web.pathParams.content
			return {
				body: {
					title:s
				},
				status: 500,
				headers:["Content Type: applocation/xml","Encoding: UTF8"]
			}
		}
	}
})(hot);
