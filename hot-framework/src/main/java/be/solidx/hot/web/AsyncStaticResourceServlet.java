package be.solidx.hot.web;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.web.context.support.WebApplicationContextUtils;

import be.solidx.hot.spring.config.CommonConfig;
import be.solidx.hot.spring.config.HotConfig;
import be.solidx.hot.utils.FileLoader;
import be.solidx.hot.utils.FileLoader.Buffer;

import com.google.common.net.HttpHeaders;
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import com.sun.nio.zipfs.ZipFileSystem;

public class AsyncStaticResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 3391406628540589609L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncStaticResourceServlet.class);
	
	ApplicationContext applicationContext;

	private ExecutorService eventLoop;
	
	private FileLoader fileLoader;
	
	private HotConfig hotConfig;
	
	private CommonConfig commonConfig;
	
	Map<URI, FileSystem> jarFileSystemCache = new HashMap<>();
	
	Map<String, byte[]> transpiledScriptCache = new ConcurrentHashMap<>();
	
	ConcurrentHashMap<URL, URL> jbossHackMap = new ConcurrentHashMap<>();
	
	@Override
	protected synchronized void doGet(HttpServletRequest servletRequest, HttpServletResponse resp) throws ServletException, IOException {
		
		AsyncContext async = servletRequest.startAsync();
		
		if (eventLoop == null) {
			try {
				applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
				eventLoop = (ExecutorService) applicationContext.getBean("staticResourcesEventLoop");
				fileLoader = applicationContext.getBean(FileLoader.class);
				commonConfig = applicationContext.getBean(CommonConfig.class);
				hotConfig = applicationContext.getBean(HotConfig.class);
			} catch (BeansException e) {
				LOGGER.error("",e);
			}
		}
		
		try {
			String requestPath = servletRequest.getRequestURL().toString().toLowerCase();
			if (requestPath.endsWith(".js") || requestPath.endsWith(".js.map")) {
				asyncLoadResource(servletRequest, resp, "text/javascript", async);
			} else if (requestPath.endsWith(".html") || servletRequest.getPathInfo().equals("/")) {
				asyncLoadResource(servletRequest, resp, "text/html; charset=utf-8", async);
			} else if (requestPath.endsWith(".css") || requestPath.endsWith(".css.map")) {
				asyncLoadResource(servletRequest, resp, "text/css; charset=utf-8", async);
			} else if (requestPath.endsWith(".png")) {
				asyncLoadResource(servletRequest, resp, "image/png", async);
			} else if (requestPath.endsWith(".jpg") || requestPath.endsWith(".jpeg")) {
				asyncLoadResource(servletRequest, resp, "image/jpg", async);
			} else if (requestPath.endsWith(".woff")) {
				asyncLoadResource(servletRequest, resp, "application/font-woff", async);
			} else if (requestPath.endsWith(".otf")) {
				asyncLoadResource(servletRequest, resp, "font/opentype", async);
			} else if (requestPath.endsWith(".ttf")) {
				asyncLoadResource(servletRequest, resp, "application/x-font-ttf", async);
			} else if (requestPath.endsWith(".eot")) {
				asyncLoadResource(servletRequest, resp, "application/vnd.ms-fontobject", async);
			} else if (requestPath.endsWith(".svg")) {
				asyncLoadResource(servletRequest, resp, "image/svg+xml", async);
			} else if (requestPath.endsWith(".swf")) {
				asyncLoadResource(servletRequest, resp, "application/x-shockwave-flash", async);
			} else if (requestPath.endsWith(".appcache")) {
				asyncLoadResource(servletRequest, resp, "application/x-shockwave-flash", async);
			} else {
				resp.setStatus(HttpStatus.NOT_FOUND.value());
				writeBytesToResponse(resp,(requestPath+ " not found").getBytes());
				async.complete();
			}
		} catch (Exception e) {
			
			writeBytesToResponse(resp,extractStackTrace(e).getBytes());
			async.complete();
		}
	}
	
	private URL getResource (String path) throws MalformedURLException, IOException, URISyntaxException {
		if (commonConfig.jboss()) {
			URL vfsURL = getClass().getResource(path);
			URL jbosshackURL = jbossHackMap.get(vfsURL);
			if (jbosshackURL == null) {
				// JBOSS VFS Bug (retrieving physical path via getFile().toURI() is buggy) Hack
				String vfsRootUrl = vfsURL.getPath().split("/WEB-INF")[0];
				String realUrl = applicationContext.getResource(vfsURL.toString()).getFile().toURI().getPath().replaceFirst(vfsRootUrl, "");
				jbosshackURL = new URL("file:"+realUrl);
				jbossHackMap.put(vfsURL, jbosshackURL);
				return jbosshackURL;
			} else return jbosshackURL;
		}
		return getClass().getResource(path);
	}

	private URL getResourceURL(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
		URL resourceUrl;
		String acceptEncoding = servletRequest.getHeader("Accept-Encoding");
		// no resource input => index.html
		if (servletRequest.getPathInfo().equals("/")) {
			if (!hotConfig.isDevMode() && acceptEncoding != null && acceptEncoding.contains("gzip")) {
				resourceUrl = getResource("/index.html.gz");
				if (resourceUrl != null) {
					servletResponse.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
					return resourceUrl;
				}
			}
			resourceUrl = getClass().getResource("/index.html");
		} else {
			if (!hotConfig.isDevMode() && acceptEncoding != null && acceptEncoding.contains("gzip")) {
				try {
					String gzPathInfo = servletRequest.getPathInfo() + ".gz";
					resourceUrl = getResource(gzPathInfo);
					if (resourceUrl != null) {
						servletResponse.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
						return resourceUrl;
					}
				} catch (Exception e) {
					LOGGER.error("Failed to build gz path info",e);
				}
			}
			resourceUrl = getResource(servletRequest.getPathInfo());
		}
		return resourceUrl;
	}
	
	protected void asyncLoadResource (
			final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final String contentType,
			final AsyncContext async) throws Exception {
		
		URL resourceUrl = getResourceURL(servletRequest,servletResponse);
		
		if (resourceUrl == null) {
			servletResponse.setStatus(HttpStatus.NOT_FOUND.value());
			async.complete();
			return;
		}
		final URI uri = resourceUrl.toURI();
		
		// Extract Servlet response of spring response wrapper if needed
		final ServletOutputStream outputStream;
		if (servletResponse instanceof SaveContextOnUpdateOrErrorResponseWrapper) {
			outputStream = ((SaveContextOnUpdateOrErrorResponseWrapper) servletResponse).getResponse().getOutputStream();
		} else {
			outputStream = servletResponse.getOutputStream();
		}
		
		eventLoop.execute(new Runnable() {
			@Override
			public void run() {
				try {
					servletResponse.setHeader(com.google.common.net.HttpHeaders.CONTENT_TYPE, contentType);
					Path path = getPath(uri);
					// If file in ZIP, we load fully load it in memory and write it directly 
					if (!commonConfig.jboss() && path.getFileSystem() instanceof ZipFileSystem) {
						byte[] bytes = Files.readAllBytes(path);
						servletResponse.setStatus(HttpStatus.OK.value());
						writeBytesToResponseAsync(outputStream, bytes, async);
					} else {
						Promise<Void, Exception, Buffer> promise = fileLoader.loadResourceAsync(path)
							.fail(new FailCallback<Exception>() {
								@Override public void onFail(Exception result) {
									if (!servletResponse.isCommitted()) {
										servletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
									}
									try {
										outputStream.write(extractStackTrace(result).getBytes());
									} catch (IOException e) {
										LOGGER.error("",e);
									} finally {
										async.complete();
									}
								}
							});
						writeBytesToResponseAsync(outputStream, promise, async);
					}
				} catch (Exception e) {
					
					LOGGER.error("Error",e);
					servletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
					writeBytesToResponseAsync(outputStream, extractStackTrace(e).getBytes(), async);
				}
			}
		});
	}
	
	
	
	private void writeBytesToResponse(HttpServletResponse httpServletResponse, byte[] bytes) {
		try {
			httpServletResponse.getOutputStream().write(bytes);
		} catch (IOException e) {
			LOGGER.error("",e);
		}
	}
	
	private void writeBytesToResponseAsync(final ServletOutputStream outputStream, byte[] bytes, final AsyncContext async) {

		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		
		outputStream.setWriteListener(new WriteListener() {
			@Override
			public void onWritePossible() throws IOException {
				eventLoop.execute(new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buffer = new byte[2048];
							int len = 0;
							while (outputStream.isReady() && (len = bais.read(buffer)) != -1) {
								outputStream.write(buffer, 0, len);
							}
							if (len <= 0) {
								async.complete();
							}
						} catch (IOException e) {
							LOGGER.error("", e);
							async.complete();
						}
					}
				});
			}
			@Override
			public void onError(Throwable t) {
				LOGGER.error("", t);
				async.complete();
			}
		});
	}
	
	private void writeBytesToResponseAsync(
			final ServletOutputStream outputStream, 
			final Promise<Void, Exception, Buffer> promise, 
			final AsyncContext async) {
		
		final LinkedList<Buffer> queue = new LinkedList<>();
		
		promise.progress(new ProgressCallback<FileLoader.Buffer>() {
			@Override public void onProgress(Buffer progress) {
				queue.add(progress);
			}
		});
		
		outputStream.setWriteListener(new WriteListener() {
			@Override
			public void onWritePossible() throws IOException {
				eventLoop.execute(new Runnable() {
					@Override
					public void run() {
						try {
							while (outputStream.isReady() && !queue.isEmpty()) {
								Buffer buffer = queue.poll();
								outputStream.write(buffer.getContent(), 0, buffer.getLength());
							}
							if (queue.isEmpty() && (promise.isResolved() || promise.isRejected())) {
								async.complete();
							}
						} catch (IOException e) {
							LOGGER.error("", e);
							async.complete();
						}
					}
				});
			}
			
			@Override
			public void onError(Throwable t) {
				LOGGER.error("", t);
				async.complete();
			}
		});
	}
	
	protected String extractStackTrace (Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}
	
	private Path getPath(URI uri) throws IOException, URISyntaxException {
		
		if (uri.toString().startsWith("zip:") || uri.toString().startsWith("jar:")) {
			FileSystem fs;
			final String[] tokens = uri.toString().split("!");
			URI jarURI = URI.create("file:" + tokens[0].split(":")[1]);
			if (jarFileSystemCache.keySet().contains(jarURI)) 
				fs = jarFileSystemCache.get(jarURI);
			else {
				fs = FileSystems.newFileSystem(Paths.get(jarURI), null);
				jarFileSystemCache.put(jarURI, fs);
			}
			return fs.getPath(tokens[1]);
		}
		return Paths.get(uri);
	}
}
