import hot.Response
import be.icode.hot.promises.groovy.GroovyDeferred



show.rest.get("/secure-scells").auth("ADMIN").then({
	sleep 2000
	return [scells:[
		title:"baskets", 
		description:'dslfjksdlfjslfjslfdjslfd'
		]
	]
})