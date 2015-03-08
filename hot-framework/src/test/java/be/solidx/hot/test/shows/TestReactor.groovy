package be.solidx.hot.test.shows;

import static org.junit.Assert.*
import groovy.transform.CompileStatic

import java.util.concurrent.Executors

import org.junit.Test

import reactor.core.Environment
import reactor.core.Reactor
import reactor.core.spec.Reactors
import reactor.spring.core.task.RingBufferAsyncTaskExecutor
import be.solidx.hot.groovy.GroovyScriptExecutor
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.python.PythonScriptExecutor
import be.solidx.hot.shows.groovy.GroovyShow
import be.solidx.hot.shows.javascript.JSShow

import com.lmax.disruptor.YieldingWaitStrategy


@CompileStatic
class TestReactor {

	GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor()
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor()
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
	
	@Test
	public void testReactor() {
		
		Environment env = new Environment()
		
		RingBufferAsyncTaskExecutor rbate = new RingBufferAsyncTaskExecutor(env)
			.setName("ringBufferExecutor")
			.setBacklog(2048)
			.setWaitStrategy(new YieldingWaitStrategy());
		rbate.afterPropertiesSet();
		
		RingBufferAsyncTaskExecutor rbate2 = new RingBufferAsyncTaskExecutor(env)
			.setName("ringBufferExecutor2")
			.setBacklog(2048)
			.setWaitStrategy(new YieldingWaitStrategy());
		rbate2.afterPropertiesSet();
		
		Reactor reactor = Reactors.reactor().env(env).get()
		
		JSShow jsShow = new JSShow(
			getClass().getResource("/reactor-show.js"), 
			rbate2, 
			Executors.newFixedThreadPool(10),
			null,
			jsScriptExecutor, 
			Executors.newScheduledThreadPool(1), 
			reactor)
		
		GroovyShow groovyShow = new GroovyShow(
			getClass().getResource("/reactor-show.groovy"),
			rbate,
			Executors.newFixedThreadPool(10),
			null,
			groovyScriptExecutor,
			Executors.newScheduledThreadPool(1),
			reactor);
		
		sleep 10000
	}

}
