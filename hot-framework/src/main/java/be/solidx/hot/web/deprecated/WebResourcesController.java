package be.solidx.hot.web.deprecated;

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
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.Script;
import be.solidx.hot.js.transpilers.CoffeeScriptCompiler;
import be.solidx.hot.js.transpilers.LessCompiler;

@Controller
public class WebResourcesController extends HotControllerImpl {
	
	private static final Log logger = LogFactory.getLog(WebResourcesController.class);
	
	@Autowired
	private CoffeeScriptCompiler coffeeScriptCompiler;
	
	@Autowired
	private GroovyController groovyController;
	
	@Autowired
	private JSController jsController;
	
	@Autowired
	private PythonController pythonController;
	
	@Autowired
	private LessCompiler lessCompiler;
	
	@RequestMapping
	public ResponseEntity<String> fallbackMapping(WebRequest webRequest, HttpServletRequest servletRequest, HttpServletResponse response) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			String resource = IOUtils.toString(loadResource("index.html"),"UTF-8");
			headers.add("Content-Type", "text/html; charset=utf-8");
			return new ResponseEntity<String>(resource, headers, HttpStatus.OK);
		} catch (Exception e) {
			try {
				return groovyController.printHotPage(webRequest, "index.hotg");
			} catch (Exception e1) {
				try {
					return jsController.printHotPage(webRequest, "index.hotr");
				} catch (Exception e2) {
					try {
						return pythonController.printHotPage(webRequest, "index.hotp");
					} catch (Exception e3) {
						return printErrorPage("No default page found. Please create a page called " +
								"index.html or index.hotg or index.hotr or index.hotp");
					}
				}
			}
		}
	}
	
	@RequestMapping ("/**/*.css")
	public ResponseEntity<String> getCSSResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		try {
			String resource = IOUtils.toString(loadResource(servletRequest.getPathInfo().substring(1)),"UTF-8");
			headers.add("Content-Type", "text/css; charset=utf-8");
			return new ResponseEntity<String>(resource, headers, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("",e);
			headers.add("Content-Type", "text/plain; charset=utf-8");
			return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping ("/**/{scriptName}.less")
	public ResponseEntity<String> getLESSResource (@PathVariable String scriptName, HttpServletRequest servletRequest) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		try {
			Script<String> script = new Script<String>(IOUtils.toByteArray(loadResource(servletRequest.getPathInfo().substring(1))), scriptName);
			headers.add("Content-Type", "text/css; charset=utf-8");
			return new ResponseEntity<String>(lessCompiler.compile(script),headers,HttpStatus.OK);
		} catch (Exception e) {
			logger.error("",e);
			headers.add("Content-Type", "text/plein; charset=utf-8");
			return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
		}
	}
	
	private void getJsResources (@PathVariable String fileNames, HttpServletRequest servletRequest, HttpServletResponse response) throws IOException{
		try {
			String[] files = fileNames.split(",");
			OutputStream out = response.getOutputStream();
			for (String filename : files) {
				out.write(";".getBytes());
				be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(filename),out);
				out.write("\n".getBytes());
			}
			out.close();
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
		
	}
	
	@RequestMapping ("/**/*.js")
	public void getJsResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		if (servletRequest.getPathInfo().startsWith("/cat/")) {
			getJsResources(servletRequest.getPathInfo().substring(5),servletRequest, response);
		} else {
			try {
				response.setCharacterEncoding("UTF-8");
				be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)),response.getOutputStream());
			} catch (Exception e) {
				logger.error("",e);
				response.setHeader("Content-Type", "text/plein");
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.getWriter().println(super.extractStackTrace(e));
			}
		}
	}
	
	@RequestMapping ("/**/{scriptName}.coffee")
	public void getCoffieResource (@PathVariable String scriptName, HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setCharacterEncoding("UTF-8");
			Script<String> script = new Script<String>(IOUtils.toByteArray(loadResource(servletRequest.getPathInfo().substring(1))), scriptName);
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(IOUtils.toInputStream(coffeeScriptCompiler.compile(script), "UTF-8"),response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.html")
	public void getHTMLResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)),response.getOutputStream());
		} catch (Exception e) {
			super.printErrorPage(e, response.getWriter());
		}
	}
	
	@RequestMapping ("/**/*.png")
	public void getPNGResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "image/png");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.jpg")
	public void getJPGResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "image/jpeg");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.gif")
	public void getGIFResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "image/gif");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.woff")
	public void getWoff (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "application/font-woff");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.otf")
	public void getOtf (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "font/opentype");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.ttf")
	public void getTTF (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "application/x-font-ttf");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.eot")
	public void getEOT (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "application/vnd.ms-fontobject");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.svg")
	public void getSVG (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			response.setHeader("Content-Type", "image/svg+xml");
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*.swf")
	public void getSwf (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		try {
			be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
		} catch (Exception e) {
			logger.error("",e);
			response.setHeader("Content-Type", "text/plein");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().println(super.extractStackTrace(e));
		}
	}
	
	@RequestMapping ("/**/*rest.js")
	public ResponseEntity<?> getRestConfigResource (HttpServletRequest servletRequest) throws IOException {
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping ("*.appcache")
	public void getAppcacheResource (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
		response.setHeader("Content-Type", "text/cache-manifest");
		be.solidx.hot.utils.IOUtils.toOutputStreamBuffered(loadResource(servletRequest.getPathInfo().substring(1)), response.getOutputStream());
	}
	
	@RequestMapping ("/{loginPrefix}_login.html")
	public void getLoginResource (WebRequest request, @PathVariable String loginPrefix, Writer writer) throws IOException {
		try {
			writer.write(IOUtils.toString(loadResource("pages/"+loginPrefix+"_login.html")));
		} catch (Exception e) {
			super.printErrorPage(e, writer);
		}
	}
	
	public void setCoffeeScriptCompiler(CoffeeScriptCompiler coffeeScriptCompiler) {
		this.coffeeScriptCompiler = coffeeScriptCompiler;
	}
	
	public void setLessCompiler(LessCompiler lessCompiler) {
		this.lessCompiler = lessCompiler;
	}
}
