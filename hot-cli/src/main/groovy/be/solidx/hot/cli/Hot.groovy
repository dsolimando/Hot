

package be.solidx.hot.cli

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.servlet.DispatcherType

import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.SerializationConfig.Feature
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy
import org.springframework.web.servlet.DispatcherServlet

import be.solidx.hot.rest.ClientAuthServlet
import be.solidx.hot.shows.rest.RestClosureServlet;
import be.solidx.hot.spring.config.SecurityConfig
import be.solidx.hot.spring.config.ShowConfig
import be.solidx.hot.spring.config.SocialConfig
import be.solidx.hot.web.AsyncStaticResourceServlet;



public class Hot {

	static Log logger = LogFactory.getLog(Hot.class)
	
	// Folder containing hot project(s)
	def projectsFolder = ""
	
	// project name is blank when we are in project folder
	def projectName = ""
	
	public Hot (projectsFolder = "", projectName = "") {
		this.projectsFolder = projectsFolder
		this.projectName = projectName
	}

	def authenticationSQLScripts = [
		h2:		"auth/sql/h2-auth-init.sql",
		mysql:	"auth/sql/mysql-auth-init.sql"
	]

	def eclipse = { Project project ->
		// Checks project exists
		def configJson = project.config

		def eclipseProject = """
		<projectDescription>
			<name>${configJson.name}</name>
			<projects>
			</projects>
			<buildSpec>
					<buildCommand>
							<name>org.eclipse.wst.jsdt.core.javascriptValidator</name>
					</buildCommand>
					<buildCommand>
							<name>org.eclipse.jdt.core.javabuilder</name>
					</buildCommand>
			</buildSpec>
			<natures>
					<nature>org.eclipse.jdt.groovy.core.groovyNature</nature>
					<nature>org.eclipse.jdt.core.javanature</nature>
					<nature>org.eclipse.wst.jsdt.core.jsNature</nature>
			</natures>
			</projectDescription>
		"""

		def eclipseClassPath = """
		<classpath>
				<classpathentry kind="src" path="www"/>
				<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
				<classpathentry exported="true" kind="con" path="GROOVY_SUPPORT"/>
		"""
		
		def file = new File (System.getProperty ('hotdir')+"/lib")
		file.eachFile {
			eclipseClassPath+= """<classpathentry kind="lib" path="${it.path}"/> """
		}
		file = new File ("lib")
		if (file.exists()) {
			file.eachFile {
				eclipseClassPath+= """<classpathentry kind="lib" path="${it.path}"/> """
			}
			eclipseClassPath += """
							<classpathentry kind="output" path=".ide/bin"/>
					</classpath>
			"""
		}
		
		def projectFile = new File (".project")
		if (projectFile.exists()) projectFile.delete()
		projectFile << eclipseProject
		
		def classpath = new File (".classpath")
		if (classpath.exists()) classpath.delete()
		classpath << eclipseClassPath
	}
	
	def addDataSource = { Project project, dataSource ->
		// Checks project exists
		def config = project.config
		
		// Checks GAE constraints about using DB
		if (config.nature == "gae" && dataSource.type != "gae") {
			logger.error ("${dataSource.type} DB is not compatible with Google App Engine")
			return
		}
		
		// If adding gae datasource and nature is jee, we change nature to gae
		if (config.nature != "gae" && dataSource.type == "gae") {
			config.nature = "gae"
		} else if (config.nature == "gae" && dataSource.type != "gae") {
			config.nature = "jee"
		}
		
		if (config.dataSources != null) {
			config.dataSources.removeAll {
				it.name == dataSource.name || it.engine == "GAE"
			}
			config.dataSources << dataSource
		} else {
			config.putAt("dataSources", [dataSource])
		}
		project.writeConfig config
	}
	
	def removeDataSource = { Project project, name ->
		def config = project.config
		config.dataSources.removeAll {
			it.name == name
		}
		project.writeConfig config
	}
	
	def startJetty = { project, port ->
		def config = project.config
		println("Starting Hot Web Server...")
		
		Server server = new Server()
		
		ServerConnector connector = new ServerConnector(server)
		connector.setPort(port?Integer.parseInt(port):8080)
//		connector.setAcceptQueueSize(Runtime.getRuntime().availableProcessors()*2)
		server.setConnectors(connector)
		server.getThreadPool().setMaxThreads(Math.max(3, Runtime.getRuntime().availableProcessors()))
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		servletContextHandler.contextPath = "/"
		server.setHandler(servletContextHandler)
		
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext()
		if (config.authList != null && !config.authList.isEmpty()) {
			rootContext.register(ShowConfig.class,SocialConfig.class, SecurityConfig.class)
			
			ServletHolder clientAuthHolder = new ServletHolder(ClientAuthServlet.class)
			clientAuthHolder.name = "client-auth"
			servletContextHandler.addServlet(clientAuthHolder, "/client-auth/*")
			
			FilterHolder securityFilterHolder = new FilterHolder(DelegatingFilterProxy.class)
			securityFilterHolder.name  = "springSecurityFilterChain"
			securityFilterHolder.asyncSupported = true
			servletContextHandler.addFilter(securityFilterHolder, "/*", EnumSet.of(DispatcherType.ASYNC, DispatcherType.REQUEST))
		} else {
			rootContext.register(ShowConfig.class)
		}
		ContextLoaderListener listener = new ContextLoaderListener(rootContext)
		servletContextHandler.addEventListener(listener)
		
		ServletHolder restHolder = new ServletHolder(RestClosureServlet.class)
		restHolder.name = "hot-rest"
		servletContextHandler.addServlet(restHolder, "/rest/*")
		
		ServletHolder websocketHolder = new ServletHolder(DispatcherServlet.class)
		websocketHolder.name = "hot-websocket"
		websocketHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
		websocketHolder.initParameters["contextConfigLocation"] = "be.solidx.hot.spring.config.WebSocketConfig"
		servletContextHandler.addServlet(websocketHolder, "/socket/*")
		
		if (project.datastore) {
			ServletHolder datastoreHolder = new ServletHolder(DispatcherServlet.class)
			datastoreHolder.name = "hot-datastore"
			datastoreHolder.initParameters["contextClass"] = "org.springframework.web.context.support.AnnotationConfigWebApplicationContext"
			datastoreHolder.initParameters["contextConfigLocation"] = "be.solidx.hot.spring.config.DataStoreConfig"
			servletContextHandler.addServlet(datastoreHolder, "/data/*")
		}
		
		ServletHolder staticHolder = new ServletHolder(AsyncStaticResourceServlet.class)
		staticHolder.name = "hot-static"
		servletContextHandler.addServlet(staticHolder, "/*")

		server.start()
		println("Hot Server started successfully, Please go to http://localhost:${port?:8080}/welcome.hotg")
		println("To kill me, just CTRL-C")
		server.join()
	}
	
	def prepareWar = { Project project ->
		// Checks project exists
		def config = project.config
		
		// remove build folder if exists
		File build = project.buildFolder
		if (build.exists()) build.deleteDir()
		
		// create empty folder
		build.mkdir()
		
		def staticDir = new File (project.buildFolder,"static")
		staticDir.mkdir()

		// copy JSP
		new File(project.absolutePaths.www).listFiles(
			[accept:{file-> file ==~ /.*?\.jsp/ }] as FileFilter
		  ).toList().each {
		  FileUtils.copyFileToDirectory it, staticDir
		}
		
		// create WEB-INF folder
		def webinf = new File (build,"WEB-INF")
		webinf.mkdir()
		
		// create web.xml webapp descriptor
		def webxml = new File (webinf, "web.xml")
		def webxmltpl = getClass().getResourceAsStream("/jee/web-tpl.xml").text.trim()
		if (config.authList && !config.authList.isEmpty()) {
			webxmltpl = getClass().getResourceAsStream("/jee/web-tpl-auth-contextLoaderListener.xml").text.trim() + webxmltpl
		} else {
			webxmltpl = getClass().getResourceAsStream("/jee/web-tpl-contextLoaderListener.xml").text.trim() + webxmltpl
		}
		def restds = config.dataSources.find {
			it.rest == true
		}
		if (restds)
			webxmltpl += "\n\n"+getClass().getResourceAsStream("/jee/web-tpl-datastore.xml").text.trim()
		webxml.text = """
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="3.1">
			${webxmltpl}
</web-app>
		""".trim()
		
		// Make a copy of resources
		def warClassesFolder = new File (build,"classes")
		warClassesFolder.mkdir()
		FileUtils.copyDirectory new File (project.absolutePaths.www), warClassesFolder,[accept:{file-> !(file ==~ /.*?\.jsp/) }] as FileFilter
		FileUtils.copyDirectory new File (project.absolutePaths.resources), warClassesFolder
		
		def sqlFolder = new File (project.absolutePaths.sql)
		if (sqlFolder.exists()) {
			def buildSql = new File (warClassesFolder,"sql")
			if (!buildSql.exists())
				buildSql.mkdir()
			FileUtils.copyDirectory sqlFolder, buildSql
		}
		
		// if gae project, create gae config file and use real gae datasource-factory
		def wardir = false
		if (config.nature == "gae") {
			wardir = true
			if (System.getenv().get("GAE_HOME") == null) {
				logger.error "please define GAE_HOME env var in order to build war folder for gae deployment"
				return
			}
			def version = config.version?.replaceAll ("\\.", "-")
			def gaeConfig = new File (webinf,"appengine-web.xml")
			def content = """
				<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
				  <application>${config["name"]}</application>
				  <version>${version?:"0-1"}</version>
				  <sessions-enabled>true</sessions-enabled>
				  <threadsafe>true</threadsafe>
				  <system-properties>
					<property name="file.encoding" value="UTF-8" />
					<property name="groovy.source.encoding" value="UTF-8" />
					<property name="python.cachedir.skip" value="true" />
				  </system-properties>
				</appengine-web-app>
			"""
			gaeConfig.setText content.trim()
		}
		
		// change devMode
		config.devMode = false
		
		// Create log4j.xml
		def logFile = new File (warClassesFolder.path+"/log4j.xml")
		logFile.text = """
<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
	<appender name="console" class="org.apache.log4j.ConsoleAppender">
		<param name="Target" value="System.out" />
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern" value="%19d{ISO8601} [%t] [ %-5p ] [ %C{1} ] [%l] - %m%n" />
		</layout>
	</appender>

	<logger name="org.springframework" additivity="false">
		<level value="debug" />
		<appender-ref ref="console" />
	</logger>

	<logger name="be.solidx.hot" additivity="false">
		<level value="debug" />
		<appender-ref ref="console" />
	</logger>

	<logger name="org.eclipse.jetty" additivity="false">
		<level value="info" />
		<appender-ref ref="console" />
	</logger>

	<root>
		<level value="off" />
	</root>
</log4j:configuration>

		"""
		// ant war task
		def params = [
			"buildDir":	project.buildFolder.name,
			appName: 	config['name'],
			staticPath:	staticDir.path,
			webxmlPath: webxml.getPath(),
			resources:	warClassesFolder.path,
			hotdir:		System.getProperty ('hotdir'),
			target:		wardir?"buildWardir":"buildWar",
			"webinf":	webinf.path
			]
		
		// write war hot config file
		project.toJSON(new File(warClassesFolder.path+"/"+Project.CONFIG_FILENAME),config)
		params
	}
	
	def subscript (scriptResourcePath, parameters) {
		ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");
		groovyEngine.put "properties", parameters
		groovyEngine.eval(getClass().getClassLoader().getResourceAsStream(scriptResourcePath).text);
	}
	
	def handleArgs (args) {
		
		def commandUsage = '''
usage: hot <command> <options>
 commands:
	create:			Create a new project.
	update:			Update an existing project.
	eclipse:		Add eclipse project nature.
	mysql:			Add mysql database to your web app.
	pgsql:			Add PostgreSQL database to your web app.
	oracle:			Add Oracle database to your web app.
	db2:			Add DB2 database to your web app.
	hsqldb:			Add HSQLDB database to your web app.
	mongo:			Add MongoDB datastore to your web app.
	restds:			Add REST datastore to a predefined database.
	rmdb:			Remove database support from your app.
	auth-db:		Add/remove database based authentication to your web app.
	auth-ldap:		Add/remove ldap based authentication to your web app.
	auth-facebook:		Add/remove facebook based authentication to your web app.
	auth-twitter:		Add/remove twitter based authentication to your web app.
	auth-google:		Add/remove google based authentication to your web app.
	auth-facebook-client:	Add/remove facebook client side authentication to your web app.
	auth-google-client:	Add/remove google client side authentication to your web app.
	
	run:			Start the dev web server (make sure to be in the project root dir).
	war:			Build the war artifact for déploying on app server.
	help:			Print this help.
		'''
					
		if (args.length == 0) {
			println commandUsage
			return
		}
		
		switch (args[0]) {
		case "create":
			def cli = new CliBuilder(usage: "hot create -n <project_name> [-t project_type] [-v version]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the project", required:true)
			cli.t(args:1,longOpt:"type","Nature of the project. Possible values: jee, gae", required:false)
			cli.v(args:1,longOpt:"version","Version number of the project", required:false)
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) {
				Log.info commandUsage
				return
			}
			if (options.n) {
				def project = new Project (options.n, projectsFolder)
				project.createProject(options.t?:"jee",options.v?:"0.1")
				println ("Project ${options.n} has been created in ${options.n} directory")
			}
			break
		
		case "update":
			def cli = new CliBuilder(usage: "hot update -n <project_name> -v <version>", posix:false)
			cli.n(args:1,longOpt:"name","Name of the project", required:true)
			cli.v(args:1,longOpt:"version","Version number of the project", required:true)
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) {
				Log.info commandUsage
				return
			}
			if (options.n || options.v ) {
				Project project = new Project(projectName, projectsFolder)
				project.update options.n, options.v
				println ("Project ${options.n} has been updated")
			}
			
			break

		case "mysql":
			def cli = new CliBuilder(usage: "hot mysql -n <datasource_name> -db database -u username [-h host] [-port port] [-p password]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.db(args:1,longOpt:"database","DB to connect to",required:true)
			cli.u(args:1,longOpt:"username","username used for DB connection",required:true)
			cli.h(args:1,longOpt:"host","Hostname or IP address of DB server (default: localhost)",required:false)
			cli.port(args:1,longOpt:"port","connection port of DB server (default: 3306)",required:false)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding a mysql database to your project..."
			try {
				def dataSource = [
					name: options.n,
					engine: "MYSQL",
					hostname: options.h?:"localhost",
					port: options.port?Integer.parseInt(options.port):3306,
					database: options.db,
					username: options.u,
					password: options.p?:"",
				]
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
		
			
		case "hsqldb":
			def cli = new CliBuilder(usage: "hot hsqldb -n <datasource_name> -db database [-s schema] [-u username] [-p password]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.db(args:1,longOpt:"database","DB to connect to",required:true)
			cli.s(args:1,longOpt:"schema","DB schema to connect to",required:false)
			cli.u(args:1,longOpt:"username","username used for DB connection (default: sa)",required:false)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding an embedded HSQLDB database to your project..."
			def dataSource = [
				name: options.n,
				engine: "HSQLDB",
				database:options.db,
				schema: options.s,
				username: options.u?:"sa",
				password: options.p?:"",
			]
			try {
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
		
		case "oracle":
			def cli = new CliBuilder(usage: "hot oracle -n <datasource_name> -u username -sid service -p password [-h host] [-port port] [-s schema]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.h(args:1,longOpt:"host","Hostname or IP address of DB server (default: localhost)",required:false)
			cli.port(args:1,longOpt:"port","connection port of DB server (default: 1521)",required:false)
			cli.sid(args:1,longOpt:"service","Oracle service name or SID",required:true)
			cli.s(args:1,longOpt:"schema","DB schema to connect to",required:false)
			cli.u(args:1,longOpt:"username","username used for DB connection",required:true)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding an oracle database to your project..."
			println "!!! Don't forget to add the Oracle JDBC driver to the project lib directory !!!"
			try {
				def dataSource = [
					name: options.n,
					engine: "ORACLE",
					hostname: options.h?:"localhost",
					port: options.port?Integer.parseInt(options.port):1521,
					service: options.sid,
					schema: options.s,
					username: options.u,
					password: options.p?:"",
				]
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "pgsql":
			def cli = new CliBuilder(usage: "hot pgsql -n <datasource_name> -db database -u username [-h host] [-port port] [-s schema] [-p password]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.h(args:1,longOpt:"host","Hostname or IP address of DB server (default: localhost)",required:false)
			cli.port(args:1,longOpt:"port","connection port of DB server (default: 5432)",required:false)
			cli.db(args:1,longOpt:"database","DB to connect to",required:true)
			cli.s(args:1,longOpt:"schema","Schema to be set in the search-path",required:false)
			cli.u(args:1,longOpt:"username","username used for DB connection",required:true)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding an PostgreSQL database to your project..."
			try {
				def dataSource = [
					name: options.n,
					engine: "PGSQL",
					hostname: options.h?:"localhost",
					port: options.port?Integer.parseInt(options.port):5432,
					database: options.db,
					schema: options.s,
					username: options.u,
					password: options.p?:"",
				]
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
		
		case "db2":
			def cli = new CliBuilder(usage: "hot db2 -n <datasource_name> -db database -u username [-h host] [-port port] [-s schema] [-p password]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.h(args:1,longOpt:"host","Hostname or IP address of DB server (default: localhost)",required:false)
			cli.port(args:1,longOpt:"port","connection port of DB server (default: 50000)",required:false)
			cli.db(args:1,longOpt:"database","DB to connect to",required:true)
			cli.s(args:1,longOpt:"schema","DB schema to connect to",required:false)
			cli.u(args:1,longOpt:"username","username used for DB connection",required:true)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding a DB2 database to your project..."
			println "!!! Don't forget to add the DB2 JDBC driver to the project lib directory !!!"
			try {
				def dataSource = [
					name: options.n,
					engine: "DB2",
					hostname: options.h?:"localhost",
					port: options.port?Integer.parseInt(options.port):50000,
					database: options.db,
					schema: options.s,
					username: options.u,
					password: options.p?:"",
				]
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "mongo":
			def cli = new CliBuilder(usage: "hot mongo -n <datasource_name> –db database [-h host] [-port port] [-u username] [-p password]", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			cli.h(args:1,longOpt:"host","Hostname or IP address of MongoDB server (default: localhost)",required:false)
			cli.port(args:1,longOpt:"port","connection port of MongoDB server (default: 27017)",required:false)
			cli.db(args:1,longOpt:"database","DB to connect to",required:true)
			cli.u(args:1,longOpt:"username","username used for DB connection",required:false)
			cli.p(args:1,longOpt:"password","password used for DB connection (default: empty)",required:false)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Adding MongoDB data your project..."
			try {
				def dataSource = [
					name: options.n,
					engine: "MONGODB",
					hostname: options.h?:"localhost",
					port: options.port?Integer.parseInt(options.port):27017,
					database: options.db,
					username: options.u?:"",
					password: options.p?:"",
				]
				Project project = new Project(projectName, projectsFolder)
				addDataSource project, dataSource
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "rmdb":
			def cli = new CliBuilder(usage: "hot rmdb -n <datasource_name> ", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			println "Removing database support for your webapp"
			try {
				Project project = new Project(projectName, projectsFolder)
				removeDataSource project, options.n
			} catch (e) { logger.error e.getMessage()}
			break
		
		case "restds":
			def cli = new CliBuilder(usage: "hot restds -n <datasource_name> ", posix:false)
			cli.n(args:1,longOpt:"name","Name of the datasource",required:true)
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			println "Adding REST interface on top of ${options.n} datasource"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.restds options.n
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-db":
			def cli = new CliBuilder(usage: "hot auth-db -n <datasource_name>", posix:false)
			cli.n args:1, longOpt:"name","Name of the datasource", required:false
			cli.u args:1, longOpt:"username","Default username to insert in the DB", required:false
			cli.p args:1, longOpt:"password","Default password (associated to username) to insert in the DB", required:false
			cli.roles args:1, "List of roles associatted to username", required:false
			cli.r "remove database based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			println "Adding database based authentication capabilities to the app"
			
			if (!options.r && !options.n) {
				println "error: Missing required option: n"
			}
			
			try {
				Project project = new Project(projectName, projectsFolder)
				project.authDb options.n, options.u, options.p, options.roles, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-ldap":
			def cli = new CliBuilder(usage: "hot auth-ldap -url <ldap url>", posix:false)
			cli.url args:1, "ldap url in the form of 'ldap://example.com:389/dc=example,dc=com'", required:false
			cli.udp args:1, longOpt:"user-dn-patterns","the LDAP patterns for finding the usernames", required:false
			cli.usb args:1, longOpt:"user-search-base","search base for user searches", required:false
			cli.usf args:1, longOpt:"user-search-filter","the LDAP filter used to search for users", required:false
			cli.gsb args:1, longOpt:"group-search-base","search base for group searches", required:false
			cli.gsf args:1, longOpt:"group-search-filter","the LDAP filter to search for groups", required:false
			cli.r "remove LDAP based authentication", required: false
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			if (!options) return
			
			if (!options.url && !options.r) {
				println "error: Missing required option: url"
			}
			
			println "Adding LDAP based authentication capabilities to the app"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.authLdap options.url, options.udp, options.usb, options.usf, options.gsb , options.gsf, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-facebook":
			def cli = new CliBuilder(usage: "hot auth-facebook -id <App id> -sec <App secret>", posix:false)
			cli.id args:1, longOpt:"app-id","Facebook provided application id", required:false
			cli.sec args:1, longOpt:"app-secret","Facebook provided application secret", required:false
			cli.r "remove facebook based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			
			if (!options || (!options.id && !options.sec && !options.r)) {
				logger.error "hot auth-facebook -id <App id> -sec <App secret>"
				return
			}
			
			if (!options.r && (!options.id || !options.sec)) {
				if (!options.id && options.sec)
					logger.error "error: Missing required option: id"
				else if (options.id && !options.sec)
					logger.error "error: Missing required options: sec"
				else
					logger.error "error: Missing required options: id, sec"
			}
			
			println "Adding facebook based authentication capabilities to the app"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.oauth "FACEBOOK", options.id, options.sec, options.r
			} catch (e) {logger.error e.getMessage()}
			break
		
		case "auth-twitter":
			def cli = new CliBuilder(usage: "hot auth-twitter -ck <consumer key> -cp <consumer password>", posix:false)
			cli.ck args:1, longOpt:"consumer-key","Twitter provided OAuth consumer key", required:false
			cli.cp args:1, longOpt:"consumer-password","Twitter provided OAuth consumer password", required:false
			cli.r "remove twitter based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			
			if (!options || (!options.id && !options.sec && !options.r)) {
				logger.error "hot auth-twitter -ck <consumer key> -cp <consumer password>"
				return
			}
			
			if (!options.r && (!options.ck || !options.cp)) {
				if (!options.ck && options.cp)
					logger.error "error: Missing required option: ck"
				else if (options.ck && !options.cp)
					logger.error "error: Missing required options: cp"
				else
					logger.error "error: Missing required options: ck, cp"
			}
			
			println "Adding twitter based authentication capabilities to the app"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.oauth "TWITTER", options.ck, options.cp, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-google":
			def cli = new CliBuilder(usage: "hot auth-google -id <client ID> -sec <client secret>", posix:false)
			cli.id args:1, longOpt:"client-id","The client ID you obtained from the Google Developers Console", required:false
			cli.sec args:1, longOpt:"client-secret","The client secret you obtained from the Developers Console", required:false
			cli.r "remove Google based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			
			if (!options || (!options.id && !options.sec && !options.r)) {
				logger.error "hot auth-google -id <client ID> -sec <client secret>"
				return
			}
			
			if (!options.r && (!options.id || !options.sec)) {
				if (!options.id && options.sec)
					logger.error "error: Missing required option: id"
				else if (options.id && !options.sec)
					logger.error "error: Missing required options: sec"
				else
					logger.error "error: Missing required options: id, sec"
			}
			
			println "Adding Google based authentication capabilities to the app"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.oauth "GOOGLE", options.id, options.sec, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-facebook-client":
			def cli = new CliBuilder(usage: "hot auth-facebook-client -id <App id> -sec <App secret>", posix:false)
			cli.id args:1, longOpt:"app-id","Facebook provided application id", required:false
			cli.sec args:1, longOpt:"app-secret","Facebook provided application secret", required:false
			cli.r "remove facebook based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			
			if (!options || (!options.id && !options.sec && !options.r)) {
				println "hot auth-facebook-client -id <App id> -sec <App secret>"
				return
			}
			
			if (!options.r && (!options.id || !options.sec)) {
				if (!options.id && options.sec)
					println "error: Missing required option: id"
				else if (options.id && !options.sec)
					println "error: Missing required options: sec"
				else
					println "error: Missing required options: id, sec"
			}
			
			try {
				Project project = new Project(projectName, projectsFolder)
				project.oauth "FACEBOOK_CLIENT", options.id, options.sec, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "auth-google-client":
			def cli = new CliBuilder(usage: "hot auth-google-google -id <client ID> -sec <client secret>", posix:false)
			cli.id args:1, longOpt:"client-id","The client ID you obtained from the Google Developers Console", required:false
			cli.sec args:1, longOpt:"client-secret","The client secret you obtained from the Developers Console", required:false
			cli.r "remove Google based authentication", required: false
			
			def options = cli.parse (args.length > 1?args[1..args.length-1]:[])
			
			if (!options || (!options.id && !options.sec && !options.r)) {
				println "hot auth-google-google -id <client ID> -sec <client secret>"
				return
			}
			
			if (!options.r && (!options.id || !options.sec)) {
				if (!options.id && options.sec)
					println "error: Missing required option: id"
				else if (options.id && !options.sec)
					println "error: Missing required options: sec"
				else
					println "error: Missing required options: id, sec"
			}
			
			println "Adding Google based authentication capabilities to the app"
			try {
				Project project = new Project(projectName, projectsFolder)
				project.oauth "GOOGLE_CLIENT", options.id, options.sec, options.r
			} catch (e) {logger.error e.getMessage()}
			break
			
		case "war":
			try {
				Project project = new Project(projectName, projectsFolder)
				def warProperties = prepareWar (project)
				// Execute script
				subscript "build.groovy", warProperties
			} catch (e) { logger.error e.getMessage()}
			break
			
		case "run":
			try {
				def project = new Project(projectName, projectsFolder)
				if (args.length > 1) {
					def cli = new CliBuilder(usage: "hot run [-p port]", posix:false)
					cli.p(args:1,longOpt:"port","Port web server will listen to",required:false)
					def options = cli.parse (args[1..args.length-1])
					
					startJetty project, options.p
				} else startJetty(project,null)
			} catch (e) { logger.error e.getMessage()}
			break
			
		case "help":
			println commandUsage
			break
			
		case "eclipse":
			def project = new Project(projectName, projectsFolder)
			println "adding eclipse project nature"
			eclipse project
			break
			
		case "status":
			println "Current project status"
			Project project = new Project(projectName, projectsFolder)
			println project.printJson()
			break
			
		default:
			println commandUsage
			break
		}
	}
	
	private static class Project {
		
		public static final String CONFIG_FILENAME = "config.json"
		
		static final def BUILD_FOLDERNAME = ".build"
		
		def	projectName
		def projectsFolderPath
		def projectFolderPath=""
		
		static ObjectMapper objectMapper = new ObjectMapper()
		
		def projectFolders = [
			www:			"www",
			shows:			"shows",
			work:			".work",
			classes:		".work/classes",
			resources:		".work/resources",
			sql: 			".work/sql",
			build:			".build"]
		
		static {
			objectMapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
			objectMapper.getSerializationConfig().enable(Feature.USE_ANNOTATIONS);
			objectMapper.getSerializationConfig().disable(Feature.WRITE_NULL_MAP_VALUES);
		}
		
		public Project (projectName="", projectsFolderPath = "") {
			this.projectName = projectName
			this.projectsFolderPath = projectsFolderPath
			projectFolderPath = projectsFolderPath == ""?(projectName == ""?"":(projectName+"/")):(projectsFolderPath+"/"+projectName+"/")
		}
		
		def createProject = { projectNature, projectVersion ->
			def configFile = new File ("${projectFolderPath}${CONFIG_FILENAME}")
			if (configFile.exists()) {
				logger.debug ("Project ${projectName} already exists")
				return
			}
			
			// create folder's hierarchy
			getAbsolutePaths().each { key, value ->
				new File(value).mkdir()
			}
			
			// create welcome page
			def wpage = getClass().getClassLoader().getResourceAsStream("pages/welcome.hotg").text
			def wpageFile = new File ("${absolutePaths.www}/welcome.hotg")
			wpageFile.setText wpage
			
			// create config file
			def config = ["name":projectName,"nature": projectNature, "version": projectVersion, devMode: true]
			objectMapper.writeValue(configFile, config)
		}
		
		def getAbsolutePaths() {
			[
				root:			"${projectFolderPath}",
				www:			"${projectFolderPath}${projectFolders.www}",
				shows:			"${projectFolderPath}${projectFolders.shows}",
				work:			"${projectFolderPath}${projectFolders.work}",
				classes: 		"${projectFolderPath}${projectFolders.classes}",
				resources:		"${projectFolderPath}${projectFolders.resources}",
				sql:			"${projectFolderPath}${projectFolders.sql}",
			]
		}
		
		def getConfigFile () {
			File configFile = new File ("${projectFolderPath}${CONFIG_FILENAME}")
			if (!configFile.exists()){
				throw new ProjectNotCreatedException("You must create a project before doing anything else")
			}
			return configFile
		}
		
		def getConfig () {
			objectMapper.readValue getConfigFile().text, Map.class
		}
		
		def writeConfig  = { config ->
			objectMapper.writeValue getConfigFile(), config
		}
		
		def toJSON(file, config) {
			objectMapper.writeValue file, config
		}
		
		def getBuildFolder() {
			new File ("${projectFolderPath}${BUILD_FOLDERNAME}")
		}
		
		def update = { name, version ->
			def config = getConfig()
			
			if (name) config.name = name
			if (version) config.version = version
			
			writeConfig config
		}
		
		def gae = {
			def config = getConfig()
			config.nature = "gae"
			writeConfig config
		}
		
		def datastore = {
			def config = getConfig()
			config.dataSources.find {
				it.rest == true
			}
		}
		
		def restds = { dsname ->
			def config = getConfig()
			def ds = config.dataSources.find {
				it.name == dsname
			}
			if (!ds) {
				logger.error "Failed to create REST interface, datasource ${dsname} does not exists"
				return
			}
			if (!ds.rest) {
				println "Adding REST interface to datasource ${dsname}..."
				ds.rest = true
			} else {
				println "Removing REST interface from datasource ${dsname}..."
				ds.rest = false
			}
			writeConfig config
		}
		
		def authDb = { dbname, username, password, roles, remove ->
			def config = getConfig()
			
			if (remove && config.authList) {
				config.authList.removeAll {
					println "Removing DB based authentication"
					it.type == "DB"
				}
				if (config.authList.empty) config.authList = null;
				writeConfig config
				return
			}
			
			def ds = config.dataSources.find {
				it.name == dbname
			}
			if (!ds) {
				logger.error "Failed to add authentication to the app, datasource ${dsname} does not exists"
				return
			}
			
			if (!config.authList) config.authList = []
			
			def auth = config.authList.find {
				it.type == "DB"
			}
			def newAuth = null
			if (auth) {
				logger.warn "Existing database based authentication will be overwrited"
				newAuth = auth
			} else {
				newAuth = [:]
				config.authList << newAuth
			}
			newAuth.type = "DB"
			newAuth.dbname = dbname
			newAuth.dbDefaultUsername = username?username:null
			newAuth.dbDefaultPassword = password?password:null
			newAuth.dbDefaultRoles = roles?roles.split(","):null
			
			writeConfig config
		}
		
		def authLdap (serverUrl, userDnPatterns, userSearchBase, userSearchFilter, groupSearchBase, groupSearchFilter, remove){
			def config = getConfig()
			
			if (!config.authList) config.authList = []
			
			if (remove) {
				config.authList.removeAll {
					println "Removing LDAP connection to ${it.ldapServerUrl}"
					it.type == "LDAP"
				}
				if (config.authList.empty) config.authList = null;
				writeConfig config
				return
			}
			
			def auth = config.authList.find {
				it.type == "LDAP"
			}
			def newAuth = null
			if (auth) {
				println "Existing LDAP based authentication will be overwrited"
				newAuth = auth
			} else {
				newAuth = [:]
				config.authList << newAuth
			}
			newAuth.type = "LDAP"
			newAuth.ldapServerUrl = serverUrl
			newAuth.userSearchBase = userSearchBase?userSearchBase:null
			newAuth.userSearchFilter = userSearchFilter?userSearchFilter:null
			newAuth.userDnPatterns = userDnPatterns?userDnPatterns.split(","):null
			newAuth.groupSearchBase = groupSearchBase?groupSearchBase:null
			newAuth.groupSearchFilter = groupSearchFilter?groupSearchFilter:null
			
			writeConfig config
		}
		
		def oauth = { type, consumerKey, consumerSecret, remove ->
			def config = getConfig()
			
			if (!config.authList) config.authList = []
			
			if (remove) {
				config.authList.removeAll {
					println "Removing ${type} authentication"
					it.type == type
				}
				if (config.authList.empty) config.authList = null;
				writeConfig config
				return
			}
			
			def auth = config.authList.find {
				it.type == type
			}
			
			def newAuth = null
			if (auth) {
				println "Existing ${type} based authentication will be overwrited"
				newAuth = auth
			} else {
				newAuth = [:]
				config.authList << newAuth
			}
			
			newAuth.type = type
			newAuth.consumerKey = consumerKey
			newAuth.consumerSecret = consumerSecret
			
			writeConfig config
		}
		
		def printJson = {
			configFile.text
		}
	}
	
	static def main (args) {
		def hot = new Hot()
		hot.handleArgs args
	}
}