package be.solidx.hot.cli;

public class DataSourceNotConfiguredException extends Exception {

	public DataSourceNotConfiguredException() {
	}

	public DataSourceNotConfiguredException(String message) {
		super(message);
	}

	public DataSourceNotConfiguredException(Throwable cause) {
		super(cause);
	}

	public DataSourceNotConfiguredException(String message, Throwable cause) {
		super(message, cause);
	}

}
