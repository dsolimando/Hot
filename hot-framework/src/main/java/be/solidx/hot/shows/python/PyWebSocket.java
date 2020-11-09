package be.solidx.hot.shows.python;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import org.python.core.PyDictionary;
import org.python.core.PyFunction;

import be.solidx.hot.Closure;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.shows.AbstractWebSocket;
import be.solidx.hot.shows.AbstractWebSocketHandler;

public class PyWebSocket extends AbstractWebSocket<PyFunction, PyDictionary> {

	public PyWebSocket(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Options buildOptions(PyDictionary options) {
		return new Options(options.get(OPTION_PATH).toString());
	}

	@Override
	protected AbstractWebSocketHandler<PyFunction> buildHandler(ExecutorService eventLoop) {
		return new PyHandler(eventLoop);
	}

	public class PyHandler extends AbstractWebSocketHandler<PyFunction> {

		public PyHandler(ExecutorService eventLoop) {
			super(eventLoop);
		}
		
		@Override
		protected Closure buildClosure(PyFunction pyFunction) {
			return new PythonClosure(pyFunction);
		}
	}
}
