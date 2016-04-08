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

import static org.junit.Assert.assertEquals;
import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import javax.script.CompiledScript;

import org.junit.Test;
import org.mozilla.javascript.NativeFunction;
import org.python.core.PyFunction;

import be.solidx.hot.Script;
import be.solidx.hot.groovy.GroovyScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.python.PythonScriptExecutor;

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
