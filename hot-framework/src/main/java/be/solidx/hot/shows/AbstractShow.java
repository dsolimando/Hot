package be.solidx.hot.shows;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import be.solidx.hot.nio.http.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;
import be.solidx.hot.ClosureExecutor;
import be.solidx.hot.Script;
import be.solidx.hot.ScriptExecutor;
import be.solidx.hot.data.AsyncDB;
import be.solidx.hot.data.DB;
import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.nio.http.HttpClient;
import be.solidx.hot.nio.http.Request;
import be.solidx.hot.shows.AbstractWebSocket.Options;
import be.solidx.hot.shows.WebSocket.Handler;


@SuppressWarnings("rawtypes")
public abstract class AbstractShow<CLOSURE,MAP extends Map,COMPILED_SCRIPT> implements Show<CLOSURE,MAP>, ClosureExecutor<CLOSURE>, EventBus<CLOSURE> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractShow.class);
	
	final protected URL filepath;
	
	protected ExecutorService eventLoop;
	
	protected ExecutorService blockingThreadPool;
	
	protected Rest<CLOSURE> rest;
	
	protected WebSocket<CLOSURE, MAP> websocket;
	
	protected Map<String, AsyncDB<CLOSURE,MAP>> dbs;
	
	protected ScriptExecutor<COMPILED_SCRIPT> scriptExecutor;
	
	protected ScheduledExecutorService taskManager;
	
	protected Reactor reactor;
	
	protected EventBus<CLOSURE> eventBus;
	
	protected Script<COMPILED_SCRIPT> script;
	
	protected HttpClient<CLOSURE, MAP> httpClient;
	
	List<ScheduledFuture<?>> tasks = new ArrayList<>();

	public AbstractShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingThreadPool,
			HttpClient<CLOSURE, MAP> httpClient,
			ScriptExecutor<COMPILED_SCRIPT> scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor,
			Map<String, DB<MAP>> dbs) throws IOException {
		
		this.filepath = filepath;
		this.scriptExecutor = scriptExecutor;
		this.eventLoop = eventLoop;
		this.blockingThreadPool = blockingThreadPool;
		this.taskManager = taskManager;
		this.reactor = reactor;
		this.httpClient = httpClient;
		this.eventBus = new ReactorEventBus();
		this.dbs = buildAsyncDBMap(dbs);
		initScriptContext();
	}
	
	public AbstractShow(
			URL filepath, 
			ExecutorService eventLoop, 
			ExecutorService blockingThreadPool,
			HttpClient<CLOSURE, MAP> httpClient,
			ScriptExecutor<COMPILED_SCRIPT> scriptExecutor, 
			ScheduledExecutorService taskManager,
			Reactor reactor) throws IOException {
		
		this.filepath = filepath;
		this.scriptExecutor = scriptExecutor;
		this.eventLoop = eventLoop;
		this.blockingThreadPool = blockingThreadPool;
		this.taskManager = taskManager;
		this.reactor = reactor;
		this.httpClient = httpClient;
		this.eventBus = new ReactorEventBus();
		initScriptContext();
	}
	
	
	protected byte[] loadScript() throws IOException {
		return be.solidx.hot.utils.IOUtils.loadBytesNoCache(filepath);
	}
	
	@Override
	public Rest<CLOSURE> getRest() {
		return rest;
	}
	
	@Override
	public WebSocket<CLOSURE, MAP> getWebsocket() {
		return websocket;
	}
	
	@Override
	public ScheduledFuture<?> setTimeout(final CLOSURE closure, int delay) {
		ScheduledFuture<?> task = taskManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				eventLoop.submit(new Runnable() {
					
					@Override
					public void run() {
						executeClosure(closure);
					}
				});
			}
		}, delay, TimeUnit.MILLISECONDS);
		tasks.add(task);
		return task;
	}
	
	@Override
	public void clearTimeout(ScheduledFuture<?> task) {
		task.cancel(true);
		tasks.remove(tasks.indexOf(task));
	}
	
	@Override
	public ScheduledFuture<?> setInterval(final CLOSURE closure, int interval) {
		
		ScheduledFuture<?> task = taskManager.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				eventLoop.submit(new Runnable() {
					
					@Override
					public void run() {
						executeClosure(closure);
					}
				});
			}
		}, 0, interval, TimeUnit.MILLISECONDS);
		tasks.add(task);
		return task;
	}
	
	@Override
	public void clearInterval(ScheduledFuture<?> task) {
		task.cancel(false);
		tasks.remove(tasks.indexOf(task));
	}
	
	@Override
	public void on(final String event, final CLOSURE closure) {
		reactor.on(Selectors.object(toString()+":" + event), new Consumer<Event<Object>>() {

			@Override
			public void accept(final Event<Object> event) {
				eventLoop.submit(new Runnable() {
					@Override
					public void run() {
						executeClosure(closure, event.getData());
					}
				});
			}
		});
	}
	
	@Override
	public void off(String event) {
		reactor.getConsumerRegistry().unregister(toString()+":" + event);
	}
	
	@Override
	public void trigger(String event, Object data) {
		reactor.notify(toString()+":" + event, Event.wrap(data));
	}
	
	@Override
	public EventBus<CLOSURE> getEventBus() {
		return eventBus;
	}
	Ì
	@Override
	public AsyncDB<CLOSURE,MAP> db(String dbname) {
		return dbs.get(dbname);
	}
	
	@Override
	public Request<CLOSURE, MAP> http(MAP options) throws Exception {
		return httpClient.buildRequest(options);
	}

    @Override
    public Request<CLOSURE, MAP> fetch(String url, MAP options) throws Exception {
        options.put("url", url);
        http(options);
    }

	@Override
	public AsyncDB<CLOSURE,MAP> getDb() {
		if (dbs.size() > 1) {
			throw new IllegalAccessError("More than one data source is defined in your project. " +
					"Please user the show.db(name) method to access the required datasource");
		} else if (dbs.size() == 0) {
			return null;
		} else {
			return dbs.values().iterator().next();
		}
	}
	
	protected abstract void initScriptContext() throws IOException;
	
	protected abstract Map<String, AsyncDB<CLOSURE, MAP>> buildAsyncDBMap(Map<String, DB<MAP>> dbs);
	
	public class ReactorEventBus implements EventBus<CLOSURE> {

		@Override
		public void on(final String event, final CLOSURE closure) {
			reactor.on(Selectors.object(event), new Consumer<Event<Object>>() {

				@Override
				public void accept(final Event<Object> event) {
					eventLoop.submit(new Runnable() {
						@Override
						public void run() {
							executeClosure(closure, event.getData());
						}
					});
				}
			});
		}

		@Override
		public void off(String event) {
			reactor.getConsumerRegistry().unregister(event);
		}

		@Override
		public void trigger(String event, Object data) {
			reactor.notify(event, Event.wrap(data));
		}
	}
	
	public Map<Options, Handler<CLOSURE>> reset()  {
		getRest().getRequestMappings().clear();
		reactor.getConsumerRegistry().clear();
		
		for (ScheduledFuture<?> task : tasks) {
			task.cancel(true);
		}
		tasks.clear();
		try {
			WebSocket<CLOSURE, MAP> webSocketCopy = this.websocket;
			WebSocket<CLOSURE, MAP> newWebSocket;
			initScriptContext();
			newWebSocket = this.websocket;
			this.websocket = webSocketCopy;
			return ((AbstractWebSocket<CLOSURE, MAP>)this.websocket).refreshSocket(newWebSocket);
		} catch (IOException e) {
			return new HashMap<AbstractWebSocket.Options, WebSocket.Handler<CLOSURE>>();
		} catch (ScriptException e) {
			System.out.println(e.getMessage());
			LOGGER.error("",e);
			return new HashMap<AbstractWebSocket.Options, WebSocket.Handler<CLOSURE>>();
		}
	}
	
	public void close () {
		getRest().getRequestMappings().clear();
		reactor.getConsumerRegistry().clear();
		
		for (ScheduledFuture<?> task : tasks) {
			task.cancel(true);
		}
		tasks.clear();
		((AbstractWebSocket)websocket).close();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof AbstractShow) {
			AbstractShow<CLOSURE, Map, COMPILED_SCRIPT> show = (AbstractShow<CLOSURE, Map, COMPILED_SCRIPT>) obj;
			return show.script.equals(script);
		}
		return false;
	}
	
	public URL getFilepath() {
		return filepath;
	}
	
	public Script<COMPILED_SCRIPT> getScript() {
		return script;
	}
}
