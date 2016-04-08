package be.solidx.hot.test.promises

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

import java.util.concurrent.Executors

import javax.script.CompiledScript

import org.apache.commons.io.IOUtils
import org.junit.Test

import be.solidx.hot.Script
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.groovy.GroovyDeferred
import be.solidx.hot.promises.js.JSDeferred
import be.solidx.hot.promises.python.PythonDeferred
import be.solidx.hot.python.PythonScriptExecutor

class TestPromises {

	@Test
	void testGroovyPromisesSucceed() {
		def deferred = new GroovyDeferred()
		def promise = deferred.promise()
		
		promise.done { result ->
			println result
		}
		
		promise.fail { ex ->
			print ex
		}
		
		promise.always {
			println "always"
		}
		
		promise.then { result ->
			"Filtered "+result
		}.done { result ->
			println result
		}
		
		deferred.resolve "Done"
	}
	
	@Test
	void testGroovySubPromisesSucceed() {
		
		Executors.newFixedThreadPool(1).submit(new Runnable(){
			void run () {
				
			}
		})
		
		def deferred = new GroovyDeferred()
		def promise = deferred.promise()
		
		def subDeferred = new GroovyDeferred();
		
		promise.then { result ->
			return subDeferred.promise()
		}.then { result ->
			print result.getClass()
			result + " Done"
		}.done { result ->
			println result
		}
		
		deferred.resolve "Done"
		Thread.sleep(1000)
		subDeferred.resolve "Sub"
	}
	
	@Test
	void testGroovyPromisesFailed() {
		def deferred = new GroovyDeferred()
		def promise = deferred.promise()
		
		promise.done { result ->
			println result
		}
		
		promise.fail { ex ->
			println ex
		}
		
		promise.always {
			println "always"
		}
		
		def p2 = promise.then { result ->
			"Filtered "+result
		}.done { result ->
			println result
		}.fail { e ->
			println "then "+ e
		}
		
		deferred.reject "Ooops"
	}
	
	@Test
	void testGroovyPromisesThrowExceptionInHandler() {
		def deferred = new GroovyDeferred()
		def promise = deferred.promise()
		
		promise.done { result ->
			println result
		}
		
		promise.always {
			println "always"
		}
		
		Promise p2 = promise.then { result ->
			throw new Exception("Error Man!")
		}.done { result ->
			println "P2 done " + result
		}
		
		p2.then(
			{ println "p2 then"; return 3}, 
			{ e -> println e; return 10})
		.fail({e ->
			 println e 
		})
		
		deferred.resolve "Yeah"
		print p2.state()
		
		// Yeah
		// java.lang.Exception: Error Man!
		// 10
		// always
	}
	
	@Test
	void testPrgressafterEnds() {
		def deferred = new GroovyDeferred()
		def promise = deferred.promise()
		deferred.notify("toto")
		deferred.resolve("done")
		promise.progress({
			println "progress"
		})
		promise.done { data ->
			println data
		}
	}
	
	@Test
	void testPythonPromises() {
		PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises.py"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises.py");
		pythonScriptExecutor.execute(script);
	}
	
	@Test
	void testPythonPromises2() {
		def params = [deferred:new PythonDeferred(),subdeferred:new PythonDeferred()]
		PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises2.py"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises2.py");
		pythonScriptExecutor.execute script,params;
	}
	
	@Test
	void testPythonPromises3() {
		def params = [deferred:new PythonDeferred(),subdeferred:new PythonDeferred()]
		PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises3.py"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises3.py");
		pythonScriptExecutor.execute script,params;
	}
	
	@Test
	void testJsPromises() {
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
		def params = [deferred:new JSDeferred(jsScriptExecutor.globalScope)]
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises.js"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises.js");
		jsScriptExecutor.execute(script,params);
	}
	
	@Test
	void testJsPromises2() {
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
		def params = [deferred:new JSDeferred(jsScriptExecutor.globalScope),subdeferred:new JSDeferred(jsScriptExecutor.globalScope)]
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises2.js"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises2.js");
		jsScriptExecutor.execute script,params;
	}
	
	@Test
	void testJsPromises3() {
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
		def params = [deferred:new JSDeferred(jsScriptExecutor.globalScope),subdeferred:new JSDeferred(jsScriptExecutor.globalScope)]
		String scriptString = IOUtils.toString(TestPromises.class.getResourceAsStream("/be/solidx/hot/promise/promises3.js"));
		Script<CompiledScript> script = new Script<>(scriptString.getBytes(), "promises3.js");
		jsScriptExecutor.execute script,params;
	}
}
