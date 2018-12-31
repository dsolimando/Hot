package be.solidx.hot.shows.python;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.script.CompiledScript;

import be.solidx.hot.nio.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import reactor.core.Reactor;
import be.solidx.hot.Script;
import be.solidx.hot.data.AsyncDB;
import be.solidx.hot.data.DB;
import be.solidx.hot.data.mongo.scripting.PyAsyncBasicDB;
import be.solidx.hot.data.scripting.PyAsyncDB;
import be.solidx.hot.nio.http.HttpClient;
import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.python.PythonDeferred;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.python.PythonScriptExecutor;
import be.solidx.hot.shows.AbstractShow;
import be.solidx.hot.shows.Show;

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
			if (entry.getValue() instanceof be.solidx.hot.data.jdbc.DB) {
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
	public Promise<PyFunction> Deferred() {
		return new PythonDeferred();
	}


}
