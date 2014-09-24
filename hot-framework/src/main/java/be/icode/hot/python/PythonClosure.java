package be.icode.hot.python;

import java.util.ArrayList;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import be.icode.hot.Closure;

public class PythonClosure implements Closure {

	PyFunction pyFunction;
	
	public PythonClosure(PyFunction pyFunction) {
		this.pyFunction = pyFunction;
	}

	@Override
	public Object call(Object... objects) {
		List<PyObject> inputs = new ArrayList<>();
		for (Object object : objects) {
			inputs.add(Py.java2py(object));
		}
		return pyFunction.__call__(inputs.toArray(new PyObject[]{}));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PythonClosure) {
			PythonClosure closure = (PythonClosure) obj;
			return pyFunction.func_code.hashCode() == closure.pyFunction.func_code.hashCode();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return pyFunction.func_code.hashCode();
	}
}
