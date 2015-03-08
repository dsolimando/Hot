package be.solidx.hot.cli;

public class ProjectNotCreatedException extends Exception {

	public ProjectNotCreatedException() {
	}

	public ProjectNotCreatedException(String message) {
		super(message);
	}

	public ProjectNotCreatedException(Throwable cause) {
		super(cause);
	}

	public ProjectNotCreatedException(String message, Throwable cause) {
		super(message, cause);
	}
}
