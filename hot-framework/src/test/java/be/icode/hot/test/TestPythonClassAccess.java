package be.icode.hot.test;

import java.util.HashMap;
import java.util.Map;

import javax.script.CompiledScript;

import org.junit.Assert;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyObject;

import be.icode.hot.Script;
import be.icode.hot.groovy.GroovyScriptExecutor;
import be.icode.hot.python.PythonScriptExecutor;

public class TestPythonClassAccess {

	@Test
	public void testPythonObjectAccess() {
		String code = "from be.icode.hot.test.TestPythonClassAccess import TestClass\n" +
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
//		String code = "import be.icode.hot.test.TestPythonClassAccess.TestClass\n" +
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
