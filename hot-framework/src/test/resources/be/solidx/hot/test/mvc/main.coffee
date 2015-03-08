class Resources
	constructor: () ->
		@bus = $("body")
	
	stations: (letter) ->
		$.ajax
			url: "rest/stations/#{letter}"
			dataType: "json"
			success: (response) =>
				event = jQuery.Event "stationRetievalSuccess"
				event.response = response
				event.letter = letter
				@bus.trigger event
			
			error: (a,b,c) =>
				@bus.trigger "stationRetievalError"