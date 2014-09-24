def stop = false

show.setTimeout ({
	show.off "message"
	show.setTimeout({stop = true},2000)
}, 4000)

show.eventBus.trigger "cross-show-event","cross show baby!"

show.on "message", { data ->
	println data
}

while (!stop) {
	show.trigger "message","hello"
	sleep (10)
}
	