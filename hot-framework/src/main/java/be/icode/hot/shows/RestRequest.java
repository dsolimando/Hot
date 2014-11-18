package be.icode.hot.shows;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerMapping;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

import com.google.common.net.HttpHeaders;

public abstract class RestRequest<T extends Map<?, ?>> {
	
	T pathParams;
	
	Principal principal;
	
	T headers;
	
	T requestParams;
	
	Object requestBody;
	
	HttpDataDeserializer httpDatadeSerializer;
	
	byte[] body;
	
	protected Authentication authentication;
	
	@SuppressWarnings("unchecked")
	public RestRequest(
			Options options,
			ScriptMapConverter<T> scriptMapConverter,
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body) {
		
		this.httpDatadeSerializer = httpDataDeserializer;
		
		Map<String, MultiValueMap<String, String>> matrixVariables = (Map<String, MultiValueMap<String, String>>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		
		pathParams = scriptMapConverter.toScriptMap(matrixVariables);
		requestParams = scriptMapConverter.toScriptMap(httpServletRequest.getParameterMap());
		headers = scriptMapConverter.httpHeadersToMap(httpServletRequest);
		principal = buildPrincipal(httpServletRequest);
		requestBody = deserializeBody(body, options);
	}
	
	public T getPathParams() {
		return pathParams;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public T getHeaders() {
		return headers;
	}

	public T getRequestParams() {
		return requestParams;
	}

	public Object getRequestBody() {
		return requestBody;
	}
	
	public abstract T getUser();

	public static class Principal {
		
		String name;

		public Principal(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	private Principal buildPrincipal(HttpServletRequest webRequest) {
		if (webRequest.getUserPrincipal() != null) {
			return new Principal(webRequest.getUserPrincipal().getName());
		}
		return null;
	}
	
	private String extractContentTypeHttpHeader () {
		for (Entry<?, ?> entry : headers.entrySet()) {
			if (entry.getKey().equals(HttpHeaders.CONTENT_TYPE)) {
				return (String) ((List<?>)entry.getValue()).get(0);
			}
		}
		return "text/plain; charset=utf-8";
	}
	
	private Object deserializeBody(byte[] body, Options options) {
		if (body == null) return null;
		
		String contentType = extractContentTypeHttpHeader();
		
		if (options.isProcessRequestData()) {
			return httpDatadeSerializer.processRequestData(body, contentType);
		} else {
			return new String(body);
		}
	}
}
