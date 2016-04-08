package be.solidx.hot.test.shows;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
