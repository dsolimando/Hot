package be.solidx.hot.exceptions;

public class JsonParsingException extends RuntimeException {

	private static final long serialVersionUID = 5416360377975274565L;

	public JsonParsingException() {
	}

	public JsonParsingException(String message) {
		super(message);
	}

	public JsonParsingException(Throwable cause) {
		super(cause);
	}

	public JsonParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
