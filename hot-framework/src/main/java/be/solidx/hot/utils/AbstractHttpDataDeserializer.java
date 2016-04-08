package be.solidx.hot.utils;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
