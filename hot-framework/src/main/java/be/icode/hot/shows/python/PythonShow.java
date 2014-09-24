package be.icode.hot.shows.python;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.script.CompiledScript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import reactor.core.Reactor;
import be.icode.hot.Script;
import be.icode.hot.data.AsyncDB;
import be.icode.hot.data.DB;
import be.icode.hot.data.mongo.scripting.PyAsyncBasicDB;
import be.icode.hot.data.scripting.PyAsyncDB;
import be.icode.hot.nio.http.HttpClient;
import be.icode.hot.promises.Promise;
import be.icode.hot.promises.python.PythonDeferred;
import be.icode.hot.python.PythonClosure;
import be.icode.hot.python.PythonScriptExecutor;
import be.icode.hot.shows.AbstractShow;
import be.icode.hot.shows.Show;

public class PythonShow extends AbstractShow<PyFunction, PyDictionary, CompiledScript> {

	private static final Log LOG = LogFactory.getLog(PythonShow.class);
	
	public PythonShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingTaskThreadPool, 
			HttpClient<PyFunction, PyDictionary> httpClient,
			PythonScriptExecutor scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor) throws IOException {
		super(filepath, eventLoop, blockingTaskThreadPool, httpClient, scriptExecutor, taskManager, reactor);
		this.scriptExecutor = scriptExecutor;
	}
	
	public PythonShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingTaskThreadPool, 
			HttpClient<PyFunction, PyDictionary> httpClient,
			PythonScriptExecutor scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor,
			Map<String, DB<PyDictionary>> pythonDbMap) throws IOException {
		super(filepath, eventLoop, blockingTaskThreadPool, httpClient, scriptExecutor, taskManager, reactor, pythonDbMap);
		this.scriptExecutor = scriptExecutor;
	}
	
	@Override
	protected void initScriptContext() throws IOException {
		Map<String, Object> context = new HashMap<>();
		rest = new PythonRest(eventLoop);
		websocket = new PyWebSocket(eventLoop);
		context.put("show", (Show<PyFunction,PyDictionary>)this);
		context.put("rest", rest);
		context.put("websocket", websocket);
		Script<CompiledScript> script = new Script<CompiledScript>(loadScript(), new File(filepath.getPath()).getName());
		scriptExecutor.execute(script, context);
	}

	@Override
	public Object executeClosure(PyFunction closure, Object... args) {
		try {
			return closure.__call__(Py.java2py(args));
		} catch (Exception e) {
			LOG.error("Error during closure execution",e);
			return null;
		}
	}
	
	@Override
	protected Map<String, AsyncDB<PyFunction, PyDictionary>> buildAsyncDBMap(Map<String, DB<PyDictionary>> dbs) {
	
		if (dbs == null) return null;
		
		Map<String, AsyncDB<PyFunction, PyDictionary>> adbs = new HashMap<>();
		for (Entry<String, DB<PyDictionary>> entry : dbs.entrySet()) {
			if (entry.getValue() instanceof be.icode.hot.data.jdbc.DB) {
				adbs.put(entry.getKey(), new PyAsyncDB(entry.getValue(), blockingThreadPool, eventLoop));
			} else {
				adbs.put(entry.getKey(), new PyAsyncBasicDB(entry.getValue(), blockingThreadPool, eventLoop));
			}
		}
		return adbs;
	}
	
	@Override
	public Promise<PyFunction> blocking(PyFunction closure) {
		final PythonDeferred jsDeferred = new PythonDeferred();
		final PythonClosure jsClosure = new PythonClosure(closure);
		
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
}
