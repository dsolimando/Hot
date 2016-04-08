package be.solidx.hot.rest;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class HttpRequest implements HttpServletRequest {

	Map<String, Object> attributes = new HashMap<String, Object>();
	
	Map<String, String[]> parameters = new HashMap<String, String[]>();
	
	String servletPath;
	
	String protocol;
	
	String servername;
	
	String contextPath;
	
	int port;
	
	String contentType;
	
	String  uri;
	
	String queryString;
	
	String path;
	
	String method;
	
	Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
	
	public HttpRequest(URL url, String contentType, String method, Map<String, Enumeration<String>> headers, String servletPath) {
		protocol = url.getProtocol();
		port = url.getPort();
		servername = url.getHost();
		path = url.getPath();
		String[] path = url.getPath().split("/");
		contextPath = path[1];
		uri = url.getPath();
		queryString = "";
		this.contentType = contentType;
		this.method = method;
		this.headers = headers;
		this.servletPath = servletPath;
		
		if (url.getQuery() == null) return;
		
		String[] params = url.getQuery().split("&");
		for (String param : params) {
			String[] eq = param.split("=");
			parameters.put(eq[0], new String[]{eq[1]});
		}
	}
	
	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return "UTF8";
	}

	@Override
	public int getContentLength() {
		return 100000;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return "192.168.0.1";
	}

	@Override
	public String getLocalName() {
		return servername;
	}

	@Override
	public int getLocalPort() {
		return port;
	}

	@Override
	public Locale getLocale() {
		return Locale.FRANCE;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return new Vector(Arrays.asList(Locale.FRANCE)).elements();
	}

	@Override
	public String getParameter(String key) {
		return parameters.get(key)[0];
	}

	@Override
	public Map<String,String[]> getParameterMap() {
		return parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return parameters.values().toArray(new String[0]);
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		return "http://hot.solidx.be/mywebapp/index.html";
	}

	@Override
	public String getRemoteAddr() {
		return "192.168.0.2";
	}

	@Override
	public String getRemoteHost() {
		return "machine.solidx.be";
	}

	@Override
	public int getRemotePort() {
		return 32000;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return servername;
	}

	@Override
	public int getServerPort() {
		return port;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
	}

	@Override
	public String getAuthType() {
		return "basic";
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		return System.currentTimeMillis();
	}

	@Override
	public String getHeader(String name) {
		String header = "";
		String separator = "";
		Enumeration<String> headerValues = headers.get(name);
		if (headerValues == null) return null;
		while (headerValues.hasMoreElements()) {
			header+=separator + headerValues.nextElement();
			separator = ",";
		}
		return header;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Vector<String> v = new Vector<>();
		for (String name : headers.keySet()) {
			v.add(name);
		}
		return v.elements();
	}

	@Override
	public Enumeration<String> getHeaders(String headerName) {
		return headers.get(headerName);
	}

	@Override
	public int getIntHeader(String name) {
		String value = headers.get(name).nextElement();
		if (value == null) return -1;
		return Integer.parseInt(value);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return getPathTranslated();
	}

	@Override
	public String getPathTranslated() {
		String queryString = this.queryString.equals("")?"":"/"+this.queryString;
		String contextPath = this.contextPath.equals("")?"":"/"+this.contextPath;
		return path.replaceAll(servletPath, "").replaceAll(queryString, "").replaceAll(contextPath, "");
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return "";
	}

	@Override
	public String getRequestURI() {
		return uri;
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer().append(protocol)
		.append("://")
		.append(servername)
		.append(":")
		.append(port)
		.append("/")
		.append(contextPath)
		.append(servletPath)
		.append(getPathInfo());
	}

	@Override
	public String getRequestedSessionId() {
		return "0";
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return true;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}
