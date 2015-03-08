package be.solidx.hot.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.exceptions.JsonParsingException;
import be.solidx.hot.exceptions.ScriptException;

import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public abstract class RestController {

	private static final Log logger = LogFactory.getLog(RestController.class);
	
	protected static final String DEFAULT_CHARSET = "utf-8";
	protected static final String DEFAULT_ACCEPT = MediaType.TEXT_PLAIN.toString();
	
	
	protected ObjectMapper objectMapper;
	
	protected ResponseEntity<byte[]> buildJSONResponse (Object response, HttpStatus httpStatus) {
		return buildJSONResponse(response,new HttpHeaders(), httpStatus);
	}
	
	protected ResponseEntity<byte[]> buildJSONResponse (Object response, HttpHeaders headers, HttpStatus httpStatus) {
		HttpHeaders jsonHeaders = jsonResponseHeaders();
		jsonHeaders.putAll(headers);
		try {
			return new ResponseEntity<byte[]>(objectMapper.writeValueAsBytes(response), jsonHeaders, httpStatus);
		} catch (Exception e) {
			return new ResponseEntity<byte[]>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> readJson (String json) {
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw new JsonParsingException(json,e);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	protected ResponseEntity<byte[]> buildEmptyResponse (HttpStatus httpStatus) {
		return new ResponseEntity<byte[]>("".getBytes(),httpStatus);
	}
	
	protected ResponseEntity<byte[]> buildErrorResponse (Exception e) {
		logger.error("",e);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		byte[] responseBytes;
		try {
			responseBytes = stringWriter.toString().getBytes(DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e1) {
			responseBytes = stringWriter.toString().getBytes();
		}
		return new ResponseEntity<byte[]>(responseBytes, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	protected HttpHeaders jsonResponseHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.put("Content-Type", Lists.newArrayList("application/json", "UTF-8"));
		return httpHeaders;
	}
	
	protected Object serializeRequestBody (String requestBody, WebRequest webRequest) {
		try {
			boolean json = false;
			if (webRequest.getHeaderValues("Content-Type") != null) {
				for (String ct : webRequest.getHeaderValues("Content-Type")) {
					if (ct.toLowerCase().contains("application/json")) {
						json = true;
						break;
					}
				}
				if (json) {
					return readJson(requestBody);
				} else {
					return requestBody;
				}
			} else {
				return requestBody;
			}
		} catch (Exception e) {
			return requestBody;
		}
	}
	
	protected HttpHeaders buildHttpHeaders(Map headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		
		if (headers == null) {
			return httpHeaders;
		}
		for (Object objectKey : headers.keySet()) {
			if (!(objectKey instanceof String)) {
				String error = "HTTP Headers map returned by script contains keys that are not String";
				logger.error(error);
				throw new ScriptException(error);
			}
			String key = (String) objectKey;
			Object objectValue = headers.get(objectKey);
			if (objectValue instanceof String) {
				String value = (String) objectValue;
				String[] scSplittedValues = value.split(",");
				httpHeaders.put(key, Lists.newArrayList(scSplittedValues));
			} else if (objectValue instanceof List) {
				List<?> objectValues = (List<?>) objectValue;
				List<String> values = new ArrayList<String>();
				for (Object elem : objectValues) {
					if (!(elem instanceof String)) {
						String error = "HTTP Headers map returned by script contains values that are not String [key=" + key + "]";
						logger.error(error);
						throw new ScriptException(error);
					}
					values.add((String) elem);
				}
				httpHeaders.put(key, values);
			}
		}
		return httpHeaders;
	}
	
	protected ResponseEntity<byte[]> buildResponse(Map content, Map headers, Integer httpStatus, Charset requestedEncoding) {
		try {
			HttpHeaders httpHeaders = buildHttpHeaders(headers);
			List<String> contentType = httpHeaders.get(com.google.common.net.HttpHeaders.CONTENT_TYPE);
			if (contentType != null && contentType.size() > 0 && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
				return new ResponseEntity<byte[]>(objectMapper.writeValueAsBytes(content), httpHeaders, HttpStatus.valueOf(httpStatus));
			} else {
				return new ResponseEntity<byte[]>(content.toString().getBytes(requestedEncoding), httpHeaders, HttpStatus.valueOf(httpStatus));
			}
		} catch (Exception e) {
			logger.error("", e);
			return new ResponseEntity<byte[]>(e.getMessage().toString().getBytes(requestedEncoding), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected ResponseEntity<byte[]> buildResponse(byte[] response, Map headers, Integer httpStatus) {
		try {
			HttpHeaders httpHeaders = buildHttpHeaders(headers);
			return new ResponseEntity<byte[]>(response, httpHeaders, HttpStatus.valueOf(httpStatus));
		} catch (ScriptException e) {
			return new ResponseEntity<byte[]>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected ResponseEntity<byte[]> buildResponse(byte[] response, HttpHeaders httpHeaders, Integer httpStatus) {
		try {
			return new ResponseEntity<byte[]>(response, httpHeaders, HttpStatus.valueOf(httpStatus));
		} catch (ScriptException e) {
			return new ResponseEntity<byte[]>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
