package be.solidx.hot.exceptions;

public class HotDBMetadataRetrievalException extends HotDataException {

	private static final long serialVersionUID = -2963532253886985240L;

	public HotDBMetadataRetrievalException() {
		super();
	}

	public HotDBMetadataRetrievalException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HotDBMetadataRetrievalException(String message) {
		super(message);
	}

	public HotDBMetadataRetrievalException(Throwable throwable) {
		super(throwable);
	}
}
