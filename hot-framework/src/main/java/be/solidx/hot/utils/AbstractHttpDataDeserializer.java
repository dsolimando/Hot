package be.solidx.hot.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public abstract class AbstractHttpDataDeserializer implements HttpDataDeserializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpDataDeserializer.class);
	
	ObjectMapper objectMapper;
	
	ParameterParser parameterParser = new ParameterParser();
	
	public AbstractHttpDataDeserializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object processRequestData (byte[] data, String contentType) {
		MediaType ct = MediaType.parseMediaType(contentType);
		
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Content-type: "+contentType);
		
		Charset charset = ct.getCharSet() == null?Charset.forName("UTF-8"):ct.getCharSet();
		
		// Subtypes arre used because of possible encoding definitions
		if (ct.getSubtype().equals(MediaType.APPLICATION_OCTET_STREAM.getSubtype())) {
			return data;
		} else if (ct.getSubtype().equals(MediaType.APPLICATION_JSON.getSubtype())) {
			try {
				return fromJSON(data);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e.getCause());
			}
		} else if (ct.getSubtype().equals(MediaType.APPLICATION_XML.getSubtype())) {
			try {
				return toXML(data, ct.getCharSet() == null?Charset.forName("UTF-8"):ct.getCharSet());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e.getCause());
			}
		} else if (ct.getSubtype().equals(MediaType.APPLICATION_FORM_URLENCODED.getSubtype())) {
			String decoded;
			try {
				decoded = URLDecoder.decode(new String(data,charset),charset.toString());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e.getCause());
			}
			return fromFormUrlEncoded(decoded);
		}
		else {
			return new String(data,ct.getCharSet() == null?Charset.forName("UTF-8"):ct.getCharSet());
		}
	}
	
	protected abstract Object toXML(byte[] data, Charset charset);

	protected Map<?, ?> fromJSON(byte[] data) throws IOException {
		return objectMapper.readValue(data, Map.class);
	}
	
	protected Map<?, ?> fromFormUrlEncoded(String data) {
		return parameterParser.parse(data,'&');
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
	
	public static void main(String[] args) {
		System.out.println(MediaType.parseMediaType("application/json").getSubtype());
	}
}
