package be.solidx.hot.shows.groovy;

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

import groovy.lang.Closure;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.shows.AbstractWebSocket;
import be.solidx.hot.shows.AbstractWebSocketHandler;
import be.solidx.hot.shows.WebSocket;

public class GroovyWebSocket extends AbstractWebSocket<Closure<?>, Map<String,Object>> {

	public GroovyWebSocket(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected AbstractWebSocketHandler<Closure<?>> buildHandler(ExecutorService eventLoop) {
		return new GroovyHandler(eventLoop);
	}	
	
	@Override
	protected be.solidx.hot.shows.AbstractWebSocket.Options buildOptions(Map<String, Object> options) {
		return new Options((String) options.get(WebSocket.OPTION_PATH));
	}
	
	public class GroovyHandler extends AbstractWebSocketHandler<Closure<?>> {

		public GroovyHandler(ExecutorService eventLoop) {
			super(eventLoop);
		}
		
		@Override
		protected be.solidx.hot.Closure buildClosure(Closure<?> closure) {
			return new GroovyClosure(closure);
		}
	}
}
