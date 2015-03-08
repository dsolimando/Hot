package be.solidx.hot.exceptions;

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = -6053764149659622124L;

	public ScriptException() {
	}

	public ScriptException(String arg0) {
		super(arg0);
	}

	public ScriptException(Throwable arg0) {
		super(arg0);
	}

	public ScriptException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
