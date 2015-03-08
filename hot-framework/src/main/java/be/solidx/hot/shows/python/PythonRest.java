package be.solidx.hot.shows.python;

import java.util.concurrent.ExecutorService;

import org.python.core.PyFunction;

import be.solidx.hot.Closure;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.shows.AbstractRest;

public class PythonRest extends AbstractRest<PyFunction> {

	public PythonRest(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Closure buildShowClosure(PyFunction closure) {
		return new PythonClosure(closure);
	}
}
