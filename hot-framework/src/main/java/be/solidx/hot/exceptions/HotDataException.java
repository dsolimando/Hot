package be.solidx.hot.exceptions;

public class HotDataException extends RuntimeException {

	private static final long serialVersionUID = -840869458802830505L;

	public HotDataException() {
		super();
	}

	public HotDataException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HotDataException(String message) {
		super(message);
	}

	public HotDataException(Throwable throwable) {
		super(throwable);
	}
}
