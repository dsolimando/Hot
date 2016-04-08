package be.solidx.hot.shows.javascript;

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

import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.Closure;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.shows.AbstractWebSocket;
import be.solidx.hot.shows.AbstractWebSocketHandler;
import be.solidx.hot.shows.WebSocket;

public class JSWebsocket extends AbstractWebSocket<NativeFunction, NativeObject> {

	Scriptable globalScope;
	
	public JSWebsocket(ExecutorService eventLoop, Scriptable globalScope) {
		super(eventLoop);
		this.globalScope = globalScope;
	}

	@Override
	protected Options buildOptions(NativeObject optionsMap) {
		return new Options((String) optionsMap.get(WebSocket.OPTION_PATH));
	}

	@Override
	protected AbstractWebSocketHandler<NativeFunction> buildHandler(ExecutorService	eventLoop) {
		return new JSHandler(eventLoop);
	}

	public class JSHandler extends AbstractWebSocketHandler<NativeFunction> {

		public JSHandler(ExecutorService eventLoop) {
			super(eventLoop);
		}
		
		@Override
		protected Closure buildClosure(NativeFunction closure) {
			return new JSClosure(closure, globalScope);
		}
	}
}
