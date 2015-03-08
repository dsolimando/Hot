package be.solidx.hot.test.shows;

import static org.junit.Assert.*

import groovy.transform.CompileStatic;

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

import org.junit.Test

import be.solidx.hot.groovy.GroovyScriptExecutor
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.python.PythonScriptExecutor
import be.solidx.hot.shows.groovy.GroovyShow


@CompileStatic
class TestTaskManager {

	GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor()
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor()
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
	
	@Test
	public void testTaskManager() {
		ExecutorService tmExecutorService = Executors.newSingleThreadExecutor()
		ScheduledExecutorService sec = Executors.newScheduledThreadPool(1);
		GroovyShow groovyShow = new GroovyShow(
			getClass().getResource("/task-manager-show.groovy"),
			Executors.newFixedThreadPool(1), 
			Executors.newFixedThreadPool(10),
			null,
			groovyScriptExecutor,
			sec, 
			null);
		
		sleep 10000
		
		assert new File(System.getProperty("java.io.tmpdir")+"/delay.txt").text == "delayed task"
		assert new File(System.getProperty("java.io.tmpdir")+"/cron.txt").text == "3"
		
		new File(System.getProperty("java.io.tmpdir")+"/delay.txt").delete()
		new File(System.getProperty("java.io.tmpdir")+"/cron.txt").delete()
	}

}
