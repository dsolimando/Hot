package be.solidx.hot.python;

import java.util.ArrayList;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.solidx.hot.Closure;

public class PythonClosure implements Closure {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PythonClosure.class);

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
		PyObject response = pyFunction.__call__(inputs.toArray(new PyObject[]{}));
		if (response instanceof PyString) {
			// We try to decode PyString in UTF-8
			try {
				return ((PyString) response).decode("UTF-8");
			} catch (Exception e) {
				LOGGER.error("",e);
			}
		}
		return response;
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
