package be.icode.hot.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import be.icode.hot.Script;
import be.icode.hot.js.transpilers.CoffeeScriptCompiler;
import be.icode.hot.js.transpilers.JsTranspiler;
import be.icode.hot.js.transpilers.LessCompiler;
import be.icode.hot.utils.FileLoader;
import be.icode.hot.utils.FileLoader.Buffer;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

@Controller
public class TranspiledScriptsController extends AsyncStaticResourcesController {

	ExecutorService blockingTaskThreadPool;
	
	LessCompiler lessCompiler;
	
	CoffeeScriptCompiler coffeeScriptCompiler;
	
	Map<String, byte[]> transpiledScriptCache = new ConcurrentHashMap<>();
	
	public TranspiledScriptsController(
			FileLoader fileServer, 
			ExecutorService blockingTaskThreadPool,
			ExecutorService eventLoop,
			LessCompiler lessCompiler,
			CoffeeScriptCompiler coffeeScriptCompiler,
			boolean devmode) {
		super(fileServer, eventLoop,devmode);
		this.blockingTaskThreadPool = blockingTaskThreadPool;
		this.lessCompiler = lessCompiler;
		this.coffeeScriptCompiler = coffeeScriptCompiler;
	}

			
	@RequestMapping ("/**/{scriptName}.less")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getLESSResource (
			@PathVariable final String scriptName, HttpServletRequest servletRequest) throws IOException {
		
		return getTranspiledScriptResource(scriptName, servletRequest, lessCompiler, "text/css; charset=utf-8");
	}
	
	@RequestMapping ("/**/{scriptName}.coffee")
	synchronized public DeferredResult<ResponseEntity<byte[]>> getCoffeeScriptResource (
			@PathVariable final String scriptName, HttpServletRequest servletRequest) throws IOException {
		
		return getTranspiledScriptResource(scriptName, servletRequest, coffeeScriptCompiler, "text/javascript; charset=utf-8");
	}
	
	private DeferredResult<ResponseEntity<byte[]>> getTranspiledScriptResource (
			final String scriptName, 
			final HttpServletRequest servletRequest,
			final JsTranspiler jsTranspiler,
			final String contentType) throws IOException {
		
		final DeferredResult<ResponseEntity<byte[]>> deferredResult = new DeferredResult<>();
		
		if (!devmode && transpiledScriptCache.keySet().contains(scriptName)) {
			System.out.println("=== Hitting cache ===");
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", contentType);
			deferredResult.setResult(
					new ResponseEntity<byte[]>(transpiledScriptCache.get(scriptName),headers,HttpStatus.OK));
			return deferredResult;
		}
		
		eventLoop.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Path path = Paths.get(getClass().getResource(servletRequest.getPathInfo()).toURI());
					final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					Promise<Void, Exception, Buffer> promise = fileServer.loadResourceAsync(path,false);
					
					promise.progress(new ProgressCallback<FileLoader.Buffer>() {
						public void onProgress(final Buffer progress) {
							outputStream.write(progress.getContent(), 0, progress.getLength());
						}
					}).fail(new FailCallback<Exception>() {
						public void onFail(Exception e) {
							deferredResult.setErrorResult(
									new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
						}
					}).then(new DonePipe<Void, byte[], Exception, Void>() {
	
						@Override
						public Promise<byte[], Exception, Void> pipeDone(Void voidd) {
							final Deferred<byte[], Exception, Void> deferred = new DeferredObject<>();
							final byte[] fileBytes = outputStream.toByteArray();
							
							blockingTaskThreadPool.execute(new Runnable() {
								@Override
								public void run() {
									try {
										Script<String> script = new Script<>(fileBytes, scriptName);
										final String css = jsTranspiler.compile(script);
										eventLoop.execute(new Runnable() {
											@Override
											public void run() {
												deferred.resolve(css.getBytes());
											}
										});
									} catch (Exception e) {
										deferred.reject(e);
									}
								}
							});
							return deferred.promise();
						}
					}).done(new DoneCallback<byte[]>() {
						@Override
						public void onDone(byte[] result) {
							transpiledScriptCache.put(scriptName, result);
							HttpHeaders headers = new HttpHeaders();
							headers.add("Content-Type", contentType);
							deferredResult.setResult(
									new ResponseEntity<byte[]>(result,headers,HttpStatus.OK));
						}
					}).fail(new FailCallback<Exception>() {
						@Override
						public void onFail(Exception e) {
							deferredResult.setErrorResult(
									new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
						}
					});
					
				} catch (Exception e) {
					deferredResult.setErrorResult(
							new ResponseEntity<byte[]>(extractStackTrace(e).getBytes(), HttpStatus.NOT_FOUND));
				}
			}
		});
			
		return deferredResult;
	}
}
