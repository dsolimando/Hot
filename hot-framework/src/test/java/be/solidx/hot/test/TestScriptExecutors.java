package be.solidx.hot.test;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.CompiledScript;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.javascript.NativeObject;
import org.python.core.PyString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.solidx.hot.JSR223ScriptExecutor;
import be.solidx.hot.Script;
import be.solidx.hot.ScriptExecutor;
import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.js.JSScriptExecutor;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestScriptExecutors {
	
	@Autowired
	@Qualifier ("groovyScriptExecutor")
	JSR223ScriptExecutor groovyScriptExecutor;
	
	@Autowired
	@Qualifier ("pythonScriptExecutorWithGlobalInit")
	JSR223ScriptExecutor pythonScriptExecutorWithGlobalInit;
	
	@Autowired
	@Qualifier ("pythonScriptExecutor")
	JSR223ScriptExecutor pythonScriptExecutor;
	
	@Autowired
	@Qualifier ("jS223ScriptExecutor")
	JSR223ScriptExecutor js223ScriptExecutor;
	
	@Autowired
	@Qualifier ("jSScriptExecutor")
	JSScriptExecutor jsScriptExecutor;
	
	@Autowired
	@Qualifier ("jSScriptExecutorWithGlobalInit")
	JSScriptExecutor jsScriptExecutorWithGlobalInit;
	
	@Test
	public void testJSScriptExecutor1() throws Exception {
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
		Script<org.mozilla.javascript.Script> script = new Script<org.mozilla.javascript.Script>("var i = 1; i++; hprint (i); i;".getBytes(), "test1");
		Double result = (Double) jsScriptExecutor.execute(script);
		Assert.assertEquals(2, result.longValue());
	}
	
	@SuppressWarnings("rawtypes")
	private Collection<Long> multiThreadedTest (final Script script, final int max, final ScriptExecutor scriptExecutor) throws InterruptedException {
		final int iterations = 100;
		ExecutorService executor = Executors.newFixedThreadPool(8);
		final ConcurrentHashMap<String, Long> results = new ConcurrentHashMap<String, Long>();
		final ConcurrentHashMap<String, Long> avgs = new ConcurrentHashMap<String, Long>();
		long benchStart = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Runnable runnable = new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						long res = 0;
						Map<String, Object> parameters = new HashMap<String, Object>();
						parameters.put("i", new Integer(max));
						parameters.put("n", new Integer(0));
						
						//long starting = System.currentTimeMillis();
						Object object = scriptExecutor.execute(script, parameters);
						if (object instanceof Bindings) {
							Bindings bindings = (Bindings) object;
							res = (Integer) bindings.get("result");
							bindings.clear();
						}
						else if (object instanceof Double) {
							res = Math.round((Double)object);
						} else if (object instanceof Long) {
							res = (long) object;
						} else res = new Long ((Integer)object);
						long end = System.currentTimeMillis() - avgs.get(this.toString());
						results.put(UUID.randomUUID().getLeastSignificantBits()+"", res);
						avgs.put(this.toString(), end);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			avgs.put(runnable.toString(), System.currentTimeMillis());
			executor.submit(runnable);
		}
		
		while (results.size() < iterations) {
			Thread.sleep(50);
		}
		//Thread.sleep(20000);
		double sum = 0;
		for (Long value : avgs.values()) {
			sum += value;
		}
		System.out.println((sum/(double)iterations)+"");
		System.out.println("==== Time needed for all requests: "+(System.currentTimeMillis() - benchStart));
		results.remove("avg");
		executor = null;
		return results.values();
	}
	
	@Test
	public void testGroovyExecutor1() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/groovy-script.groovy")), "test_"+Thread.currentThread().getName());
		Collection<Long> results = multiThreadedTest(script,1000000, groovyScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			System.out.println(result);
			Assert.assertEquals(1000000, r.longValue());
		}
	}
	
	@Test
	public void testGroovyExecutor2() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/groovy-script2.groovy")), "test_"+Thread.currentThread().getName());
		//groovyScriptExecutor.compile(script);
		Collection<Long> results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		results = multiThreadedTest(script,1000, groovyScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			//System.out.println(result);
			Assert.assertEquals(1000, r.longValue());
		}
	}
	
	@Test
	public void testPythonExecutor2() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/python-script2.py")), "test_"+Thread.currentThread().getName());
		// Python interpreter is 3X faster when compiling scripts
		Collection<Long> results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		results = multiThreadedTest(script,1000, pythonScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			Assert.assertEquals(1000, r.longValue());
		}
	}
	
	@Test
	public void testJSExecutor2() throws Exception {
		Script<org.mozilla.javascript.Script> script = new Script<org.mozilla.javascript.Script>(IOUtils.toByteArray(getClass().getResourceAsStream("/js-script2.js")), "test_"+Thread.currentThread().getName());
		// Python interpreter is 3X faster when compiling scripts
		Collection<Long> results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		results = multiThreadedTest(script,1000, jsScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			Assert.assertEquals(1000, r.longValue());
		}
	}
	
	@Test
	public void testJS223Executor2() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/js-script2.js")), "test_"+Thread.currentThread().getName());
		// Python interpreter is 3X faster when compiling scripts
		Collection<Long> results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		results = multiThreadedTest(script,1000, js223ScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			Assert.assertEquals(1000, r.longValue());
		}
	}
	
	@Test
	public void testPythonExecutor1() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/python-script.py")), "test_"+Thread.currentThread().getName());
		Collection<Long> results = multiThreadedTest(script,100000, pythonScriptExecutor);
		results = multiThreadedTest(script,100000, pythonScriptExecutor);
		results = multiThreadedTest(script,100000, pythonScriptExecutor);
		for (Object result : results) {
			Long r = (Long) result;
			System.out.println(result);
			Assert.assertEquals(100000, r.longValue());
		}
	}
	
	@Test
	public void testGroovyExecutorSequential() throws Exception {
		final ScriptExecutor<CompiledScript> scriptExecutor = groovyScriptExecutor;
		double sum = 0;
		for (int i = 0; i < 100; i++) {
			Integer j = 0;
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("i", j);
			parameters.put("tname", Thread.currentThread().getName());
			
			try {
				Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/groovy-script.groovy")), "test_"+Thread.currentThread().getName());
				long starting = System.currentTimeMillis();
				Integer res = (Integer) scriptExecutor.execute(script, parameters);
				long end = System.currentTimeMillis() - starting;
				Assert.assertEquals(1000000, res.longValue());
				System.out.println(end);
				sum += end;
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sum/100.);
	}
	
	@Test
	public void testPythonExecutorSequential() throws Exception {
		final ScriptExecutor<CompiledScript> scriptExecutor = pythonScriptExecutor;
		double sum = 0;
		for (int i = 0; i < 100; i++) {
			Integer j = 0;
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("i", j);
			parameters.put("tname", Thread.currentThread().getName());
			parameters.put("result", new Integer(0));
			try {
				Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/python-script.py")), "test_"+Thread.currentThread().getName());
				long starting = System.currentTimeMillis();
				Integer res = (Integer) ((Bindings) scriptExecutor.execute(script, parameters)).get("result");
				long end = System.currentTimeMillis() - starting;
				Assert.assertEquals(1000000, res.longValue());
				System.out.println(end);
				sum += end;
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sum/100.);
	}
	
	@Test
	public void testPythonExecutorSequential2() throws Exception {
		final ScriptExecutor<CompiledScript> scriptExecutor = pythonScriptExecutor;
		double sum = 0;
		for (int i = 0; i < 10000; i++) {
			Integer j = 1000;
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("i", j);
			parameters.put("tname", Thread.currentThread().getName());
			parameters.put("result", new Integer(0));
			try {
				Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/python-script2.py")), "test_"+Thread.currentThread().getName());
				long starting = System.currentTimeMillis();
				long res = (Integer) ((Bindings) scriptExecutor.execute(script, parameters)).get("result");
				long end = System.currentTimeMillis() - starting;
				Assert.assertEquals(1000, res);
				System.out.println(end);
				sum += end;
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sum/100.);
	}
	
	@Test
	public void testJSExecutorSequential2() throws Exception {
		final ScriptExecutor<org.mozilla.javascript.Script> scriptExecutor = jsScriptExecutor;
		double sum = 0;
		for (int i = 0; i < 1000; i++) {
			Integer j = 1000;
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("i", j);
			parameters.put("tname", Thread.currentThread().getName());
			parameters.put("result", new Integer(0));
			try {
				Script<org.mozilla.javascript.Script> script = new Script<org.mozilla.javascript.Script>(IOUtils.toByteArray(getClass().getResourceAsStream("/js-script2.js")), "test_"+Thread.currentThread().getName());
				long starting = System.currentTimeMillis();
				Double resDouble = (Double) scriptExecutor.execute(script, parameters);
				Long res = Math.round(resDouble);
				long end = System.currentTimeMillis() - starting;
				Assert.assertEquals(1000, res.longValue());
				System.out.println(end);
				sum += end;
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sum/1000.);
	}
	
	@Test
	public void testJSOutput() throws Exception {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Script<CompiledScript> script = new Script<CompiledScript>("print ('hello');".getBytes(), "testJSOutput");
		js223ScriptExecutor.execute(script, printWriter);
		Assert.assertEquals("hello", stringWriter.toString().trim());
	}
	
	@Test
	public void testPythonOutput() throws Exception {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Script<CompiledScript> script = new Script<CompiledScript>("print 'hello'".getBytes(), "testJSOutput");
		pythonScriptExecutor.execute(script, printWriter);
		Assert.assertEquals("hello", stringWriter.toString().trim());
	}
	
	@Test
	public void testGroovyOutput() throws Exception {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Script<CompiledScript> script = new Script<CompiledScript>("print 'hello'".getBytes(), "testJSOutput");
		groovyScriptExecutor.execute(script, printWriter);
		Assert.assertEquals("hello", stringWriter.toString());
	}
	
	@Test
	public void testTypePython1() throws Exception {
		PyString pyString = new PyString("hello");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("s", pyString);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Script<CompiledScript> script = new Script<CompiledScript>("print type(s)".getBytes(), "testJSOutput");
		pythonScriptExecutor.execute(script,map, printWriter);
		Assert.assertEquals("<type 'str'>", stringWriter.toString().trim());
	}
	
	@Test
	public void testScriptEncodingGroovy() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/frenchScript.groovy")), "french");
		FileOutputStream fileOutputStream = new FileOutputStream(new File ("testiso.txt"));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bos,"ISO8859-1");
		
		groovyScriptExecutor.execute(script,outputStreamWriter);
		outputStreamWriter.flush();
		bos.flush();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream,"ISO8859-1");
		writer.append(new String(bos.toByteArray(),"iso8859-1"));
		writer.flush();
		writer.close();
		fileOutputStream.write(outputStream.toByteArray());
		fileOutputStream.close();
	}
	
	@Test
	public void testGlobalContextInitExecutionPy() throws Exception {
		Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream("/be/solidx/hot/test/script.py")), "script.py");
		Map<String, Object> contextMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("toto", "titi");
		contextMap.put("dbmap", map);
		Bindings bindings = (Bindings) pythonScriptExecutorWithGlobalInit.execute(script, contextMap);
		Assert.assertEquals("titi", bindings.get("result"));
	}
	
	@Test
	public void testGlobalContextInitExecutionJs() throws Exception {
		Script<org.mozilla.javascript.Script> script = new Script<org.mozilla.javascript.Script>(IOUtils.toByteArray(getClass().getResourceAsStream("/be/solidx/hot/test/script.js")), "script.js");
		Map<String, Object> contextMap = new HashMap<String, Object>();
		NativeObject nativeObject = new NativeObject();
		nativeObject.put("toto",nativeObject, "titi");
		contextMap.put("dbmap", nativeObject);
		Object o = jsScriptExecutorWithGlobalInit.execute(script, contextMap);
		Assert.assertEquals("titi", o);
	}
}
