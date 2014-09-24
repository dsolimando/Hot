def ant = new AntBuilder()
def buildWar = {
	if (new File("lib").exists()) {
		ant.war (destfile:"${properties.buildDir}/${properties.appName}.war",webxml:"${properties.webxmlPath}", update:"true") {
			classes (dir:"${properties.resources}")
			fileset (dir:"${properties.staticPath}")
			lib (dir:"${properties.hotdir}/lib") {
				include (name: "spring*.jar")
				include (name: "log4j*.jar")
				include (name: "jython*.jar")
				include (name: "json-lib*.jar")
				include (name: "rhino-*.jar")
				include (name: "jackson*.jar")
				include (name: "jasper*.jar")
				include (name: "juli*.jar")
				include (name: "jdeferred*.jar")
				include (name: "json-lib*.jar")
				include (name: "ezmorph-*.jar")
				include (name: "groovy*.jar")
				include (name: "commons*.jar")
				include (name: "asm*.jar")
				include (name: "aop*.jar")
				include (name: "cglib*.jar")
				include (name: "aspectj*.jar")
				include (name: "zql*.jar")
				include (name: "hot-*.jar")
				include (name: "hsqldb*.jar")
				include (name: "gmongo*.jar")
				include (name: "guava*.jar")
				include (name: "hsqldb*.jar")
				include (name: "mongo-java-driver*.jar")
				include (name: "mysql-connector*.jar")
				include (name: "postgresql*.jar")
				exclude (name: "hot-cli*.jar")
				exclude (name: "hot-gae*.jar")
			}
			lib (dir:"lib") {
				include (name: "*.jar")
			}
			webinf (dir:"${properties.webinf}")
		}
	} else {
		ant.war (destfile:"${properties.buildDir}/${properties.appName}.war",webxml:"${properties.webxmlPath}", update:"true") {
			classes (dir:"${properties.resources}")
			fileset (dir:"${properties.staticPath}")
			lib (dir:"${properties.hotdir}/lib") {
				include (name: "spring*.jar")
				include (name: "log4j*.jar")
				include (name: "jython*.jar")
				include (name: "json-lib*.jar")
				include (name: "jasper*.jar")
				include (name: "jdeferred*.jar")
				include (name: "juli*.jar")
				include (name: "rhino-*.jar")
				include (name: "jackson*.jar")
				include (name: "json-lib*.jar")
				include (name: "ezmorph-*.jar")
				include (name: "groovy*.jar")
				include (name: "commons*.jar")
				include (name: "asm*.jar")
				include (name: "aop*.jar")
				include (name: "cglib*.jar")
				include (name: "aspectj*.jar")
				include (name: "zql*.jar")
				include (name: "hot-*.jar")
				include (name: "hsqldb*.jar")
				include (name: "gmongo*.jar")
				include (name: "guava*.jar")
				include (name: "hsqldb*.jar")
				include (name: "mongo-java-driver*.jar")
				include (name: "mysql-connector*.jar")
				include (name: "postgresql*.jar")
				exclude (name: "hot-cli*.jar")
				exclude (name: "hot-gae*.jar")
			}
			webinf (dir:"${properties.webinf}")
		}
	}
}

def buildWardir = {
	ant.war (destfile:"${properties.buildDir}/${properties.appName}.war",webxml:"${properties.webxmlPath}", update:"true") {
		classes (dir:"${properties.resources}")
		lib (dir:"${properties.hotdir}/lib") {
			include (name: "spring*.jar")
			include (name: "log4j*.jar")
			include (name: "juli*.jar")
			include (name: "jython*.jar")
			include (name: "rhino-*.jar")
			include (name: "jackson*.jar")
			include (name: "jasper*.jar")
			include (name: "jdeferred*.jar")
			include (name: "json-lib*.jar")
			include (name: "ezmorph-*.jar")
			include (name: "groovy*.jar")
			include (name: "commons*.jar")
			include (name: "asm*.jar")
			include (name: "aop*.jar")
			include (name: "cglib*.jar")
			include (name: "aspectj*.jar")
			include (name: "zql*.jar")
			include (name: "hot-*.jar")
			include (name: "hsqldb*.jar")
			include (name: "gmongo*.jar")
			include (name: "guava*.jar")
		}
		lib (dir:"lib") {
			include (name: "*.jar")
		}
		lib (dir:"${System.getenv().get('GAE_HOME')}/lib/impl") {
			include (name: "appengine-api.jar")
		}
		webinf (dir:"${properties.webinf}")
	}
	ant.unwar (src:"${properties.buildDir}/${properties.appName}.war",dest:"${properties.buildDir}/${properties.appName}")
}

switch (properties.target) {
	case "buildWar":
		buildWar ()
		break;
	
	case "buildWardir":
		buildWardir ()
		break;
}