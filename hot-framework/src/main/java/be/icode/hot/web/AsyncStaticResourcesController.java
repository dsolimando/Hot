package be.icode.hot.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import be.icode.hot.utils.FileLoader;
import be.icode.hot.utils.FileLoader.Buffer;

import com.sun.nio.zipfs.ZipFileSystem;

public abstract class AsyncStaticResourcesController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncStaticResourcesController.class);
	
	FileLoader fileServer;
	
	ExecutorService eventLoop;
	
	boolean devmode;
	
	Map<URI, FileSystem> jarFileSystemCache = new HashMap<>();
	
	public AsyncStaticResourcesController(FileLoader fileServer, ExecutorService eventLoop, boolean devmode) {
		this.fileServer = fileServer;
		this.eventLoop = eventLoop;
		this.devmode = devmode;
	}

	@RequestMapping ("/**/*.css")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getCSSResource (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "text/css; charset=utf-8");
	}
	
	@RequestMapping ("**/*.html")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getHTMLResource (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "text/html; charset=utf-8");
	}
	
	@RequestMapping ({"/**/*.png","/**/*.PNG"})
	synchronized public DeferredResult<ResponseEntity<byte[]>> getPNGResource (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "image/png");
	}
	
	@RequestMapping ({"/**/*.jpg","/**/*.jpeg","/**/*.JPG","/**/*.JPEG"})
	synchronized public DeferredResult<ResponseEntity<byte[]>> getJPGResource (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "image/png");
	}
	
	@RequestMapping ("/**/*.woff")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getWoff (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "application/font-woff");
	}
	
	@RequestMapping ("/**/*.otf")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getOtf (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "font/opentype");
	}
	
	@RequestMapping ("/**/*.ttf")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getTTF (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "application/x-font-ttf");
	}
	
	@RequestMapping ("/**/*.eot")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getEOT (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "application/vnd.ms-fontobject");
	}
	
	@RequestMapping ("/**/*.svg")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getSVG (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "image/svg+xml");
	}
	
	@RequestMapping ("/**/*.swf")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getSwf (HttpServletRequest servletRequest) throws Exception {
		return getResource(servletRequest, "application/x-shockwave-flash");
	}
	
	@RequestMapping ("/**/*rest.js")
	synchronized public ResponseEntity<?> getRestConfigResource (HttpServletRequest servletRequest) throws Exception {
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping ("*.appcache")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getAppcacheResource (HttpServletRequest servletRequest, HttpServletResponse response) throws Exception {
		return getResource(servletRequest, "text/cache-manifest");
	}
	
	@RequestMapping ("/**/*.js")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getJsResource (HttpServletRequest servletRequest) throws Exception {
		if (servletRequest.getPathInfo().startsWith("/concat/")) {
			return getJsResources(servletRequest.getPathInfo().substring(8),servletRequest);
		} else {
			return getResource(servletRequest, "text/javascript");
		}
	}
	
	protected DeferredResult<ResponseEntity<byte[]>> getResource (
			final HttpServletRequest servletRequest, final String contentType) throws IOException, URISyntaxException {
		
		final DeferredResult<ResponseEntity<byte[]>> deferredResult = new DeferredResult<>();
		
		final URI uri = getClass().getResource(servletRequest.getPathInfo()).toURI();
		
		eventLoop.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Path path = getPath(uri);
					if (path.getFileSystem() instanceof ZipFileSystem) {
						try {
							byte[] bytes = Files.readAllBytes(path);
							HttpHeaders headers = new HttpHeaders();
							headers.add(com.google.common.net.HttpHeaders.CONTENT_TYPE, contentType);
							headers.add(com.google.common.net.HttpHeaders.CONNECTION, "keep-alive");
							deferredResult.setResult(
									new ResponseEntity<>(bytes,headers,HttpStatus.OK));
						} catch (Exception e) {
							deferredResult.setErrorResult(
									new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
						}
					} else {
						Promise<Void, Exception, Buffer> promise = fileServer.loadResourceAsync(path,!devmode);
						final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						
						promise.progress(new ProgressCallback<FileLoader.Buffer>() {
							public void onProgress(Buffer progress) {
								outputStream.write(progress.getContent(), 0, progress.getLength());
							}
						}).done(new DoneCallback<Void>() {
							public void onDone(Void result) {
								HttpHeaders headers = new HttpHeaders();
								headers.add(com.google.common.net.HttpHeaders.CONTENT_TYPE, contentType);
								headers.add(com.google.common.net.HttpHeaders.CONNECTION, "keep-alive");
								deferredResult.setResult(
										new ResponseEntity<>(outputStream.toByteArray(),headers,HttpStatus.OK));
							}
						}).fail(new FailCallback<Exception>() {
							public void onFail(Exception e) {
								deferredResult.setErrorResult(
										new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
							}
						});
					}
					
					
				} catch (Exception e) {
					deferredResult.setErrorResult(
							new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.NOT_FOUND));
				}
			}
		});
		return deferredResult;
	}
	
	private Path getPath(URI uri) throws IOException {
		if (uri.toString().startsWith("jar:") && !jarFileSystemCache.keySet().contains(uri)) {
			LOGGER.warn("accessing content of jar file => blocking IO needed");
			final String[] tokens = uri.toString().split("!");
			final FileSystem fs = FileSystems.newFileSystem(URI.create(tokens[0]), new HashMap<String,Object>());
			jarFileSystemCache.put(uri, fs);
			return fs.getPath(tokens[1]);
		}
		return Paths.get(uri);
	}
	
	private DeferredResult<ResponseEntity<byte[]>> getJsResources (@PathVariable String fileNames, HttpServletRequest servletRequest) throws IOException{
		
		final DeferredResult<ResponseEntity<byte[]>> deferredResult = new DeferredResult<>();
		
		try {
			String[] files = fileNames.split(",");
			
			final List<Path> paths = new ArrayList<>();
			for (String file : files) {
				String absFile = file.startsWith("/")?file:"/"+file;
				paths.add(getPath(getClass().getResource(absFile).toURI()));
			}
			
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(";".getBytes());
			
			final ProgressCallback<Buffer> progressCallback = new ProgressCallback<FileLoader.Buffer>() {
				@Override
				public void onProgress(final Buffer progress) {
					outputStream.write(progress.getContent(), 0, progress.getLength());
				}
			};
			final FailCallback<Exception> failCallback = new FailCallback<Exception>() {
				@Override
				public void onFail(Exception e) {
					deferredResult.setErrorResult(
							new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
				}
			};
			
			eventLoop.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						AtomicInteger i = new AtomicInteger();
						Promise<Void, Exception, Buffer> currentPromise = fileServer.loadResourceAsync(paths.get(i.get()),!devmode);
						currentPromise.progress(progressCallback)
							.done(new FileReadCallback(paths, progressCallback, failCallback, i, outputStream, deferredResult, devmode))
							.fail(failCallback);
					} catch (IOException e) {
						deferredResult.setErrorResult(
								new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
					}
				}
			});

		} catch (Exception e) {
			deferredResult.setErrorResult(
					new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
		}
		return deferredResult;
	}
	
	protected String extractStackTrace (Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}
	
	private class FileReadCallback implements DoneCallback<Void> {

		List<Path> paths;
		
		ProgressCallback<Buffer> progressCallback;
		
		FailCallback<Exception> failCallback;
		
		AtomicInteger pathIndex;
		
		ByteArrayOutputStream outputStream;
		
		DeferredResult<ResponseEntity<byte[]>> deferredResult;
		
		boolean devmode;
		
		public FileReadCallback(
				List<Path> paths, 
				ProgressCallback<Buffer> progressCallback,
				FailCallback<Exception> failCallback,
				AtomicInteger pathIndex, 
				ByteArrayOutputStream outputStream,
				DeferredResult<ResponseEntity<byte[]>> deferredResult,
				boolean devmode) {
			this.paths = paths;
			this.progressCallback = progressCallback;
			this.pathIndex = pathIndex;
			this.outputStream = outputStream;
			this.deferredResult = deferredResult;
			this.devmode = devmode;
		}

		@Override
		public void onDone(Void result) {
			eventLoop.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (pathIndex.get() == paths.size()) return;
						
						outputStream.write("\n;".getBytes(),0,"\n;".length());
						Promise<Void, Exception, Buffer> promise = fileServer.loadResourceAsync(paths.get(pathIndex.get()),!devmode)
							.progress(progressCallback);
						pathIndex.incrementAndGet();
						promise.done(new FileReadCallback(paths,progressCallback, failCallback, pathIndex, outputStream,deferredResult,devmode));
						
						if (pathIndex.get() == paths.size()) {
							promise.done(new DoneCallback<Void>() {
								@Override
								public void onDone(Void result) {
									try {
										outputStream.flush();
										HttpHeaders headers = new HttpHeaders();
										headers.add("Content-Type", "text/javascript; charset=utf-8");
										deferredResult.setResult(
												new ResponseEntity<byte[]>(outputStream.toByteArray(),headers,HttpStatus.OK));
									} catch (IOException e) {}
								}
							});
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
			});
			return;
		}
	}
}
