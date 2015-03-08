package be.solidx.hot.data.scripting

import groovy.transform.CompileStatic

import java.util.concurrent.ExecutorService

import be.solidx.hot.data.DB
import be.solidx.hot.data.scripting.GroovyAsyncDB;


@CompileStatic
class GroovyAsyncDBProxy extends GroovyAsyncDB {
	
	public GroovyAsyncDBProxy(DB<Map<String, Object>> db, ExecutorService blockingTasksThreadPool, ExecutorService eventLoop) {
		super(db, blockingTasksThreadPool, eventLoop);
	}

	def getProperty (String name) {
		this.getCollection name
	}
}
