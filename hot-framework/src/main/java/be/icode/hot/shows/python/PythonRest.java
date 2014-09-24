package be.icode.hot.shows.python;

import java.util.concurrent.ExecutorService;

import org.python.core.PyFunction;

import be.icode.hot.Closure;
import be.icode.hot.python.PythonClosure;
import be.icode.hot.shows.AbstractRest;

public class PythonRest extends AbstractRest<PyFunction> {

	public PythonRest(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Closure buildShowClosure(PyFunction closure) {
		return new PythonClosure(closure);
	}
}
