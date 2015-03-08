package be.solidx.hot.shows.groovy;

import groovy.lang.Closure;

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

import reactor.core.Reactor;
import be.solidx.hot.Script;
import be.solidx.hot.ScriptExecutor;
import be.solidx.hot.data.AsyncDB;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.data.mongo.groovy.GroovyAsyncBasicDBProxy;
import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.nio.http.HttpClient;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.groovy.GroovyDeferred;
import be.solidx.hot.shows.AbstractShow;
import be.solidx.hot.shows.Show;


public class GroovyShow extends AbstractShow<Closure<?>,Map<String, Object>,CompiledScript> {
	
	private static final Log LOG = LogFactory.getLog(GroovyShow.class);
	
	public GroovyShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingThreadPool, 
			HttpClient<Closure<?>, Map<String, Object>> groovyHttpClient,
			ScriptExecutor<CompiledScript> scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor, 
			Map<String, DB<Map<String, Object>>> dbs) throws IOException {
		super(filepath, eventLoop, blockingThreadPool, groovyHttpClient, scriptExecutor, taskManager, reactor, dbs);
	}

	public GroovyShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingThreadPool, 
			HttpClient<Closure<?>, Map<String, Object>> groovyHttpClient,
			ScriptExecutor<CompiledScript> scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor) throws IOException {
		super(filepath, eventLoop, blockingThreadPool, groovyHttpClient, scriptExecutor, taskManager, reactor);
	}

	protected void initScriptContext() throws IOException {
		rest = new GroovyRest(eventLoop);
		websocket = new GroovyWebSocket(eventLoop);

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("show", (Show<Closure<?>, Map<String,Object>>)this);
		context.put("rest", rest);
		context.put("websocket", websocket);
		
		script = new Script<CompiledScript>(loadScript(), new File(filepath.getPath()).getName());
		scriptExecutor.execute(script, context);
	}

	@Override
	public Object executeClosure(Closure<?> closure, Object... args) {
		try {
			return closure.call(args);
		} catch (Exception e) {
			LOG.error("Error during closure execution",e);
			return null;
		}
	}
	
	@Override
	public Promise<Closure<?>> blocking(Closure<?> closure) {
		final GroovyDeferred groovyDeferred = new GroovyDeferred();
		final GroovyClosure groovyClosure = new GroovyClosure(closure);
		blockingThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.currentThread().setContextClassLoader(blockingThreadPool.getClass().getClassLoader());
					final Object result = groovyClosure.call();
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							groovyDeferred.resolve(result);
						}
					});
				} catch (final Exception e) {
					eventLoop.execute(new Runnable() {
						@Override
						public void run() {
							groovyDeferred.reject(e);
						}
					});
				}
			}
		});
		return groovyDeferred.promise();
	}
	
	@Override
	protected Map<String, AsyncDB<Closure<?>, Map<String, Object>>> buildAsyncDBMap(Map<String, DB<Map<String, Object>>> dbs) {
		
		if (dbs == null) return null;
		
		HashMap<String, AsyncDB<Closure<?>, Map<String, Object>>> adbs = new HashMap<>();
		for (Entry<String, DB<Map<String, Object>>> entry: dbs.entrySet()) {
			if (entry.getValue() instanceof be.solidx.hot.data.jdbc.groovy.DB) {
				adbs.put(entry.getKey(), new be.solidx.hot.data.jdbc.groovy.GroovyAsyncDBProxy ((be.solidx.hot.data.jdbc.groovy.DB) entry.getValue(), blockingThreadPool, eventLoop));
			} else {
				adbs.put(entry.getKey(), new GroovyAsyncBasicDBProxy ((BasicDB<Map<String, Object>>) entry.getValue(), blockingThreadPool, eventLoop));
			}
		}
		return adbs;
	}
}
