package be.solidx.hot.rest;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;


public class TestHttpRequest {

	@Test
	public void testRequest1() throws Exception {
		Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
		headers.put("Host", new Vector<>(Arrays.asList("agile.dzone.com")).elements());
		headers.put("Accept",new Vector<>(Arrays.asList("text/html","application/xhtml+xml","application/xml;q=0.9,*/*;q=0.8")).elements());
		headers.put("Accept-Charset", new Vector<>(Arrays.asList("ISO-8859-1","utf-8;q=0.7,*;q=0.7")).elements());
		HttpRequest httpRequest = new HttpRequest(new URL("http://hot.solidx.be:8080/app1/repository/group1/service1/1.0"),"text/json", "GET", headers,"");
		Assert.assertEquals("text/json", httpRequest.getContentType());
		Assert.assertEquals("app1", httpRequest.getContextPath());
		Assert.assertEquals("hot.solidx.be", httpRequest.getLocalName());
		Assert.assertEquals("hot.solidx.be", httpRequest.getServerName());
		Assert.assertEquals(8080, httpRequest.getServerPort());
		Assert.assertEquals(8080, httpRequest.getLocalPort());
		Assert.assertEquals("", httpRequest.getServletPath());
		Assert.assertEquals("http://hot.solidx.be:8080/app1/repository/group1/service1/1.0", httpRequest.getRequestURL().toString());
		Assert.assertEquals("http", httpRequest.getProtocol());
		Assert.assertEquals("/repository/group1/service1/1.0", httpRequest.getPathTranslated());
		Assert.assertEquals(0, httpRequest.getParameterMap().size());
		Assert.assertEquals("agile.dzone.com", httpRequest.getHeader("Host"));
		Assert.assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", httpRequest.getHeader("Accept"));
		Assert.assertEquals("ISO-8859-1,utf-8;q=0.7,*;q=0.7", httpRequest.getHeader("Accept-Charset"));
	}
	
	@Test
	public void testRequest2() throws Exception {
		Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
		headers.put("Host", new Vector<>(Arrays.asList("agile.dzone.com")).elements());
		headers.put("Accept",new Vector<>(Arrays.asList("text/html","application/xhtml+xml","application/xml;q=0.9,*/*;q=0.8")).elements());
		headers.put("Accept-Charset", new Vector<>(Arrays.asList("ISO-8859-1","utf-8;q=0.7,*;q=0.7")).elements());
		HttpRequest httpRequest = new HttpRequest(new URL("http://hot.solidx.be:8080/app1/servlet/repository/group1/service1/1.0"),"text/json", "GET", headers,"/servlet");
		Assert.assertEquals("text/json", httpRequest.getContentType());
		Assert.assertEquals("app1", httpRequest.getContextPath());
		Assert.assertEquals("hot.solidx.be", httpRequest.getLocalName());
		Assert.assertEquals("hot.solidx.be", httpRequest.getServerName());
		Assert.assertEquals(8080, httpRequest.getServerPort());
		Assert.assertEquals(8080, httpRequest.getLocalPort());
		Assert.assertEquals("/servlet", httpRequest.getServletPath());
		Assert.assertEquals("http://hot.solidx.be:8080/app1/servlet/repository/group1/service1/1.0", httpRequest.getRequestURL().toString());
		Assert.assertEquals("http", httpRequest.getProtocol());
		Assert.assertEquals("/repository/group1/service1/1.0", httpRequest.getPathTranslated());
		Assert.assertEquals(0, httpRequest.getParameterMap().size());
		Assert.assertEquals("agile.dzone.com", httpRequest.getHeader("Host"));
		Assert.assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", httpRequest.getHeader("Accept"));
		Assert.assertEquals("ISO-8859-1,utf-8;q=0.7,*;q=0.7", httpRequest.getHeader("Accept-Charset"));
	}
	
	@Test
	public void testRequest3() throws Exception {
		Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
		headers.put("Host", new Vector<>(Arrays.asList("agile.dzone.com")).elements());
		headers.put("Accept",new Vector<>(Arrays.asList("text/html","application/xhtml+xml","application/xml;q=0.9,*/*;q=0.8")).elements());
		headers.put("Accept-Charset", new Vector<>(Arrays.asList("ISO-8859-1","utf-8;q=0.7,*;q=0.7")).elements());
		HttpRequest httpRequest = new HttpRequest(new URL("http://hot.solidx.be:8080/app1/servlet/repository/group1/service1/1.0?var1=val1&var2=val2"),"text/json", "GET", headers,"/servlet");
		Assert.assertEquals("text/json", httpRequest.getContentType());
		Assert.assertEquals("app1", httpRequest.getContextPath());
		Assert.assertEquals("hot.solidx.be", httpRequest.getLocalName());
		Assert.assertEquals("hot.solidx.be", httpRequest.getServerName());
		Assert.assertEquals(8080, httpRequest.getServerPort());
		Assert.assertEquals(8080, httpRequest.getLocalPort());
		Assert.assertEquals("/servlet", httpRequest.getServletPath());
		Assert.assertEquals("http://hot.solidx.be:8080/app1/servlet/repository/group1/service1/1.0", httpRequest.getRequestURL().toString());
		Assert.assertEquals("http", httpRequest.getProtocol());
		Assert.assertEquals("/repository/group1/service1/1.0", httpRequest.getPathTranslated());
		Assert.assertEquals(2, httpRequest.getParameterMap().size());
		Assert.assertEquals("val1", httpRequest.getParameter("var1"));
		Assert.assertEquals("val2", httpRequest.getParameter("var2"));
		Assert.assertEquals("agile.dzone.com", httpRequest.getHeader("Host"));
		Assert.assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", httpRequest.getHeader("Accept"));
		Assert.assertEquals("ISO-8859-1,utf-8;q=0.7,*;q=0.7", httpRequest.getHeader("Accept-Charset"));
	}
}
