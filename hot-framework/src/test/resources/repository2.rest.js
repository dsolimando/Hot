{
	"mappings":[	
		{
			"paths":["/repository/services/{group}/{service}"],
			"methods":["GET"],
			"script":"python-script.py",
			"class": "ServiceRestAPI",
			"method": "getVersions"
		},
		{
			"paths":["/repository/services/{groupName}/{serviceName}"],
			"methods":["DELETE"],
			"script":"python-script.py",
			"class": "ServiceRestAPI",
			"method": "deleteService"
		},
		{
			"paths":["/repository/services/{groupName}/{serviceName}/{version}"],
			"methods":["DELETE"],
			"script":"python-script.py",
			"class": "ServiceRestAPI",
			"method": "deleteServiceVersion"
		},
		{
			"paths":["/repository/services/{groupName}/{serviceName}/{serviceVersion}"],
			"methods":["POST"],
			"script":"python-script.py",
			"class": "ServiceRestAPI",
			"method": "addWSDL"
		},
		{
			"paths":["/schemas/{group}/{schema}/{version}"],
			"methods":["POST"],
			"script":"python-script.py",
			"class": "SchemaRestAPI",
			"method": "addSchema"
		}
	]
}