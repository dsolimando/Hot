package be.solidx.hot.shows.javascript;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;

import reactor.core.Reactor;
import be.solidx.hot.data.AsyncDB;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.scripting.JSAsyncBasicDB;
import be.solidx.hot.data.scripting.JSAsyncDB;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.nio.http.HttpClient;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.js.JSDeferred;
import be.solidx.hot.shows.AbstractShow;
import be.solidx.hot.shows.Show;

public class JSShow extends AbstractShow<NativeFunction, NativeObject, Script> {

	private static final Log LOG = LogFactory.getLog(JSShow.class);
	
	public JSShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingTaskThreadPool,
			HttpClient<NativeFunction, NativeObject> httpClient,
			JSScriptExecutor scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor) throws IOException {
		super(filepath, eventLoop, blockingTaskThreadPool, httpClient, scriptExecutor, taskManager, reactor);
	}
	
	public JSShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingTaskThreadPool,
			HttpClient<NativeFunction, NativeObject> httpClient,
			JSScriptExecutor scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor,
			Map<String, DB<NativeObject>> dbMap) throws IOException {
		super(filepath, eventLoop,blockingTaskThreadPool, httpClient, scriptExecutor, taskManager, reactor, dbMap);
	}
	
	@Override
	protected void initScriptContext() throws IOException {
		JSScriptExecutor jsScriptExecutor = (JSScriptExecutor) scriptExecutor;
		
		rest = new JSRest(eventLoop, jsScriptExecutor.getGlobalScope());
		websocket = new JSWebsocket(eventLoop, jsScriptExecutor.getGlobalScope());
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("show", (Show<NativeFunction, NativeObject>)this);
		context.put("rest", rest);
		context.put("websocket", websocket);
		
		be.solidx.hot.Script<Script> script = new be.solidx.hot.Script<Script>(loadScript(), new File(filepath.getPath()).getName());
		jsScriptExecutor.execute(script,context);
	}
	
	@Override
	public Object executeClosure(NativeFunction closure, Object... args) {
		try {
			Object value;
			Context context = Context.enter();
			value = closure.call(context, ((JSScriptExecutor)scriptExecutor).getGlobalScope(), closure, args);
			Context.exit();
			return value;
		} catch (Exception e) {
			LOG.error("Error during closure execution",e);
			return null;
		}
	}
	
	@Override
	protected Map<String, AsyncDB<NativeFunction, NativeObject>> buildAsyncDBMap(Map<String, DB<NativeObject>> dbs) {
		Map<String, AsyncDB<NativeFunction, NativeObject>> adbs = new HashMap<>();
		
		if (dbs == null) return null;
		
		for (Entry<String, DB<NativeObject>> entry : dbs.entrySet()) {
			if (entry.getValue() instanceof be.solidx.hot.data.jdbc.DB) {
				adbs.put(entry.getKey(), new JSAsyncDB(entry.getValue(), blockingThreadPool, eventLoop, ((JSScriptExecutor)scriptExecutor).getGlobalScope()));
			} else {
				adbs.put(entry.getKey(), new JSAsyncBasicDB(entry.getValue(), blockingThreadPool, eventLoop, ((JSScriptExecutor)scriptExecutor).getGlobalScope()));
			}
		}
		return adbs;
	}
	
	@Override
	public Promise<NativeFunction> blocking(NativeFunction closure) {
		JSScriptExecutor jsScriptExecutor = (JSScriptExecutor) scriptExecutor;
		final JSDeferred jsDeferred = new JSDeferred(jsScriptExecutor.getGlobalScope());
		final JSClosure jsClosure = new JSClosure(closure,jsScriptExecutor.getGlobalScope());
		blockingThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.currentThread().setContextClassLoader(blockingThreadPool.getClass().getClassLoader());
					final Object result = jsClosure.call();
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							jsDeferred.resolve(result);
						}
					});
				} catch (final Exception e) {
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							jsDeferred.reject(e);
						}
					});
				}
			}
		});
		return jsDeferred.promise();
	}

	@Override
	public Promise<NativeFunction> Deferred() {
		JSScriptExecutor jsScriptExecutor = (JSScriptExecutor) scriptExecutor;
		return new JSDeferred(jsScriptExecutor.getGlobalScope());
	}
	
	
}
