package be.solidx.hot.data.mongo.groovy

import java.util.Map;
import java.util.concurrent.ExecutorService;

import groovy.transform.CompileStatic;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.scripting.GroovyAsyncBasicDB


@CompileStatic
class GroovyAsyncBasicDBProxy extends GroovyAsyncBasicDB {

	public GroovyAsyncBasicDBProxy(BasicDB<Map<String, Object>> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}
	
	def getProperty (String name) {
		this.getCollection name
	}
}
