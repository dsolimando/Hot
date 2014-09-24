package be.icode.hot.data.jdbc.groovy

import groovy.transform.CompileStatic

import java.util.concurrent.ExecutorService

@CompileStatic
class GroovyAsyncDBProxy extends GroovyAsyncDB {
	
	public GroovyAsyncDBProxy(DB db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	def getProperty (String name) {
		this.getCollection name
	}
}
