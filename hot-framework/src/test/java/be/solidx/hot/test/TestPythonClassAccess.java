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

import java.util.HashMap;
import java.util.Map;

import javax.script.CompiledScript;

import org.junit.Assert;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyObject;

import be.solidx.hot.Script;
import be.solidx.hot.python.PythonScriptExecutor;

public class TestPythonClassAccess {

	@Test
	public void testPythonObjectAccess() {
		String code = "from be.solidx.hot.test.TestPythonClassAccess import TestClass\n" +
				"test = TestClass()\n" +
				"test.name = 'toto'\n" +
				"result = test.yes\n";
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("result", null);
		PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();
		Script<CompiledScript> script = new Script<>(code.getBytes(), "class-access");
		vars = (Map<String, Object>) pythonScriptExecutor.execute(script, vars);
		
		Assert.assertEquals("toto", vars.get("result"));
	}
	
//	@Test
//	public void testGroovyObjectAccess() {
//		String code = "import be.solidx.hot.test.TestPythonClassAccess.TestClass\n" +
//				"def test = new TestClass()\n" +
//				"test.name = 'toto'\n" +
//				"result test.yes\n";
//		
//		Map<String, Object> vars = new HashMap<String, Object>();
//		vars.put("result", null);
//		GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor();
//		Script<CompiledScript> script = new Script<>(code.getBytes(), "class-access");
//		vars = (Map<String, Object>) groovyScriptExecutor.execute(script, vars);
//		
//		Assert.assertEquals("toto", vars.get("result"));
//	}
	
	public static class TestClass {
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public PyObject __getattr__(String name) {
			return Py.java2py(getName());
		}
		
		public Object getProperty(String name) {
			return name;
		}
	}
}
