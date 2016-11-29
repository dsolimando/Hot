package be.solidx.hot.shows;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.net.HttpHeaders;

import be.solidx.hot.shows.ClosureRequestMapping.Options;
import be.solidx.hot.shows.groovy.GroovyRestRequest;
import be.solidx.hot.utils.HttpDataDeserializer;
import be.solidx.hot.utils.ScriptMapConverter;

public abstract class RestRequest<T extends Map<?, ?>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyRestRequest.class);
	
	T pathParams;
	
	Principal principal;
	
	T headers;
	
	T requestParams;
	
	Session session;
	
	Object requestBody;
	
	String ip;
	
	HttpDataDeserializer httpDatadeSerializer;
	
	byte[] body;
	
	protected Authentication authentication;
	
	protected ScriptMapConverter<T> scriptMapConverter;
	
	protected Map<String, Object> userAsMap = new HashMap<>();

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
		session = new Session(httpServletRequest.getSession());
		ip = httpServletRequest.getRemoteAddr();
		
		headers = scriptMapConverter.httpHeadersToMap(httpServletRequest);
		principal = buildPrincipal(httpServletRequest);
		
		requestBody = deserializeBody(body, options);
		this.scriptMapConverter = scriptMapConverter;
	}
	
	protected void initUser(Authentication authentication) {
		
		if (authentication == null) {
			userAsMap = null;
			return;
		}
		
		LOGGER.debug("Authentication Type: "+authentication.getClass());
		
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			List<String> roles = new ArrayList<>();
			
			if (usernamePasswordAuthenticationToken.getPrincipal() instanceof LdapUserDetailsImpl) {
				LdapUserDetailsImpl detailsImpl = (LdapUserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
				userAsMap.put("name", detailsImpl.getUsername());
				userAsMap.put("username", detailsImpl.getUsername());
				userAsMap.put("password", detailsImpl.getPassword());
				userAsMap.put("dn", detailsImpl.getDn());
				for (GrantedAuthority authority : detailsImpl.getAuthorities()) {
					roles.add(authority.getAuthority());
				}
			} else {
				
				Object principal = usernamePasswordAuthenticationToken.getPrincipal();
				if (principal instanceof String) {
					userAsMap.put("name", usernamePasswordAuthenticationToken.getPrincipal());
					userAsMap.put("username", usernamePasswordAuthenticationToken.getPrincipal());
					userAsMap.put("password", usernamePasswordAuthenticationToken.getCredentials());
					for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
						roles.add(authority.getAuthority());
					}
				} else if (principal instanceof User) {
					User userDetails = (User) principal;
					userAsMap.put("name", userDetails.getUsername());
					userAsMap.put("username", userDetails.getUsername());
					userAsMap.put("password", userDetails.getPassword());
					for (GrantedAuthority authority : userDetails.getAuthorities()) {
						roles.add(authority.getAuthority());
					}
				}
			}
			userAsMap.put("roles", roles);
		} else if (authentication instanceof SocialAuthenticationToken) {
			SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
			ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
			userAsMap.put("name", socialAuthenticationToken.getName());
			userAsMap.put("username", socialAuthenticationToken.getName());
			userAsMap.put("accessToken", connectionData.getAccessToken());
			userAsMap.put("picture", connectionData.getImageUrl());
			userAsMap.put("link", connectionData.getProfileUrl());
			userAsMap.put("id", connectionData.getProviderUserId());
			userAsMap.put("provider", connectionData.getProviderId());
			userAsMap.put("expiresIn", connectionData.getExpireTime());
		}
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
	
	public Session getSession() {
		return session;
	}
	
	public String getIp() {
		return ip;
	}

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
	
	public static class Session {
		
		private static final String ID = "id";
		private static final String CREATION_TIME = "creation-time";
		private static final String LAST_ACCESS_TIME = "last-access-time";
		private static final String MAX_INTERVAL = "max-interval";
		
		HttpSession servletSession;
		
		public Session(HttpSession servletSession) {
			this.servletSession = servletSession;
		}
		
		public Object attribute (String name) {
			switch (name) {
			case ID:
				return servletSession.getId();
			case CREATION_TIME:
				return servletSession.getCreationTime();
			case LAST_ACCESS_TIME:
				return servletSession.getLastAccessedTime();
			case MAX_INTERVAL:
				return servletSession.getMaxInactiveInterval();

			default:
				return servletSession.getAttribute(name);
			}
		}
		
		public Object attr(String name) {
			return attribute(name);
		}
		
		public Session attribute (String name, Object value) {
			if (value == null) {
				servletSession.removeAttribute(name);
			} else if (!Arrays.asList(ID,CREATION_TIME, LAST_ACCESS_TIME, MAX_INTERVAL).contains(name)) {
				servletSession.setAttribute(name, value);
			} 
			return this;
		}
		
		public Session attr(String name, Object value) {
			attribute(name, value);
			return this;
		}
	}
}
