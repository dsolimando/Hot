package be.icode.hot.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.MediaType;

public abstract class AbstractHttpDataDeserializer implements HttpDataDeserializer {
	
	ObjectMapper objectMapper;
	
	public AbstractHttpDataDeserializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object processRequestData (byte[] data, String contentType) {
		MediaType ct = MediaType.parseMediaType(contentType);
		if (ct.getSubtype().equals(MediaType.APPLICATION_OCTET_STREAM.getSubtype())) {
			return data;
		} else if (ct.getSubtype().equals(MediaType.APPLICATION_JSON.getSubtype())) {
			try {
				return toJSON(data);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e.getCause());
			}
		} else if (ct.getSubtype().equals(MediaType.APPLICATION_XML.getSubtype())) {
			try {
				return toXML(data, ct.getCharSet() == null?Charset.forName("UTF-8"):ct.getCharSet());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e.getCause());
			}
		} else {
			return new String(data,ct.getCharSet() == null?Charset.forName("UTF-8"):ct.getCharSet());
		}
	}
	
	protected abstract Object toXML(byte[] data, Charset charset);

	protected Map<?, ?> toJSON(byte[] data) throws IOException {
		return objectMapper.readValue(data, Map.class);
	}
	
	public static class DeserializationException extends RuntimeException {

		private static final long serialVersionUID = 8262551683131621856L;

		public DeserializationException() {
			super();
		}

		public DeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public DeserializationException(String message, Throwable cause) {
			super(message, cause);
		}

		public DeserializationException(String message) {
			super(message);
		}

		public DeserializationException(Throwable cause) {
			super(cause);
		}
	}
}
