
show.setTimeout({ 
	def d = new File(System.getProperty("java.io.tmpdir")+"/delay.txt")
	d.write("delayed task")
}, 1000)

int i = 0

def t = show.setInterval({ 
	def d = new File(System.getProperty("java.io.tmpdir")+"/cron.txt")
	d.write("${i++}")
} , 1000)

sleep(3500)

println "clearing"
show.clearInterval t