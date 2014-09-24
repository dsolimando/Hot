package be.icode.hot.test;

import static org.junit.Assert.assertEquals;
import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import javax.script.CompiledScript;

import org.junit.Test;
import org.mozilla.javascript.NativeFunction;
import org.python.core.PyFunction;

import be.icode.hot.Script;
import be.icode.hot.groovy.GroovyScriptExecutor;
import be.icode.hot.js.JSScriptExecutor;
import be.icode.hot.python.PythonScriptExecutor;

public class TestClosureCodeRefresh {

	GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor();
	
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
	
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
	
	@Test
	public void testGroovyClosureEquality() throws Exception {
		Script<CompiledScript> script = new Script<>("return { a -> a+3 }".getBytes(), "closure");
		Script<CompiledScript> script2 = new Script<>("return { a -> a+3 }".getBytes(), "closure2");
		Closure<?> closure = (Closure<?>) groovyScriptExecutor.execute(script);
		Closure<?> closure2 = (Closure<?>) groovyScriptExecutor.execute(script2);
	
		assertEquals(closure.getMetaClass().hashCode(), closure2.getMetaClass().hashCode());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPythonClosureEquality() throws Exception {
		Script<CompiledScript> script = new Script<>("def a():\n\ta+3".getBytes(), "closure");
		Script<CompiledScript> script2 = new Script<>("def a():\n\ta+3".getBytes(), "closure2");
		
		Map<String, Object> context = new HashMap<>();
		Map<String, Object> context2 = new HashMap<>();
		
		context = (Map<String, Object>) pythonScriptExecutor.execute(script,context);
		context2 = (Map<String, Object>) pythonScriptExecutor.execute(script2,context);
		
		PyFunction function = (PyFunction) context.get("a");
		PyFunction function2 = (PyFunction) context2.get("a");
		
		assertEquals(function.func_code.hashCode(), function2.func_code.hashCode());
	}
	
	@Test
	public void testJSClosureEquality() throws Exception {
		Script<org.mozilla.javascript.Script> script = new Script<>("var f = function(a){ return a+1 };f".getBytes(), "closure");
		Script<org.mozilla.javascript.Script> script2 = new Script<>("var f = function(a){return a+1 };f".getBytes(), "closure2");
		
		NativeFunction function = (NativeFunction) jsScriptExecutor.execute(script);
		NativeFunction function2 = (NativeFunction) jsScriptExecutor.execute(script2);
		
		assertEquals(function.getEncodedSource().hashCode(), function2.getEncodedSource().hashCode());
	}
}
