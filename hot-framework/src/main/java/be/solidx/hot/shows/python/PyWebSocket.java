package be.solidx.hot.shows.python;

import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.solidx.hot.Closure;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.shows.AbstractWebSocket;
import be.solidx.hot.shows.AbstractWebSocketHandler;

public class PyWebSocket extends AbstractWebSocket<PyFunction, PyDictionary> {

	public PyWebSocket(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Options buildOptions(PyDictionary options) {
		return new Options(options.get(OPTION_PATH).toString());
	}

	@Override
	protected AbstractWebSocketHandler<PyFunction> buildHandler(ExecutorService eventLoop) {
		return new PyHandler(eventLoop);
	}

	public class PyHandler extends AbstractWebSocketHandler<PyFunction> {

		public PyHandler(ExecutorService eventLoop) {
			super(eventLoop);
		}
		
		@Override
		protected Closure buildClosure(PyFunction pyFunction) {
			return new PythonClosure(pyFunction);
		}
	}
}