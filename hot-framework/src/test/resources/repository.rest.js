{
	"mappings":[
	{
		"paths":["/repository/groups"],
		"methods":["GET"],
		"headers":["Content-Type: application/json"],
		"script":"python-script.py",
		"class": "GroupRestAPI",
		"method": "getGroups"
	},
	{
		"paths":["/repository/groups/{groupName}"],
		"methods":["POST"],
		"script":"python-script.py",
		"class": "GroupRestAPI",
		"method": "saveGroup"
	},
	{
		"paths":["/repository/services/{groupName}/{service}/{version}"],
		"methods":["GET"],
		"headers":["Accept=application/json"],
		"script":"python-script.py",
		"class": "ServiceRestAPI",
		"method": "getService"
	},
	{
		"paths":["/repository/services/{groupName}/{service}/{version}"],
		"methods":["GET"],
		"script":"python-script.py",
		"class": "ServiceRestAPI",
		"method": "getWSDL"
	},
	{
		"paths":["/repository/services/{group}"],
		"methods":["GET"],
		"script":"python-script.py",
		"method": "getServicesGroup"
	}
	]
}
