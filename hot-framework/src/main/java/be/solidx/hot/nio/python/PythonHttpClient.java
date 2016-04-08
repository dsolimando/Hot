package be.solidx.hot.nio.python;

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

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.python.core.PyString;
import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.nio.HttpClient;

public class PythonHttpClient extends HttpClient<PyFunction,PyDictionary> {

	public PythonHttpClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(eventLoopPool, defaultConversionService);
	}

	public PythonHttpClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
	}

	@Override
	protected Request buildRequest(PyDictionary options, PyFunction requestClosure) {
		return new PyRequest(options, requestClosure);
	}

	@Override
	protected Response<PyFunction,PyDictionary> buildResponse() {
		return new PyResponse();
	}

	public class PyRequest extends Request {

		public PyRequest(PyDictionary options, PyFunction requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected RequestClosure<PyFunction,PyDictionary> buildRequestClosure(PyFunction requestClosure) {
			return new PyRequestClosure(requestClosure);
		}

		@Override
		protected RequestErrorClosure<PyFunction> buildRequestErrorClosure(PyFunction requestErrorClosure) {
			return new PyRequestErrorClosure(requestErrorClosure);
		}

		@Override
		protected RequestCanceledClosure<PyFunction> buildRequestCanceledClosure(PyFunction requestCanceledClosure) {
			return new PyRequestCanceledClosure(requestCanceledClosure);
		}
		
	}
	
	static class PyRequestClosure implements RequestClosure<PyFunction,PyDictionary> {

		PyFunction requestClosure;
		
		public PyRequestClosure(PyFunction requestClosure) {
			this.requestClosure = requestClosure;
		}

		@Override
		public void call(Response<PyFunction,PyDictionary> response) {
			requestClosure.__call__(Py.java2py(response));
		}
		
	}
	
	static class PyRequestErrorClosure implements RequestErrorClosure<PyFunction> {

		PyFunction requestErrorClosure;
		
		public PyRequestErrorClosure(PyFunction requestErrorClosure) {
			this.requestErrorClosure = requestErrorClosure;
		}

		@Override
		public void call(String message) {
			requestErrorClosure.__call__(new PyString(message));
		}
	}
	
	static final class PyRequestCanceledClosure implements RequestCanceledClosure<PyFunction> {

		PyFunction requestCanceledClosure;
		
		public PyRequestCanceledClosure(PyFunction requestCanceledClosure) {
			this.requestCanceledClosure = requestCanceledClosure;
		}

		@Override
		public void call(String message) {
			requestCanceledClosure.__call__(new PyString(message));
		}
	}
	
	public static class PyResponse extends Response<PyFunction,PyDictionary>{

		@Override
		protected ResponseClosure<PyFunction> buildResponseClosure(PyFunction pyFunction) {
			return new PyResponseClosure(pyFunction);
		}

		@Override
		protected ResponseEndClosure<PyFunction> buildResponseEndClosure(PyFunction pyFunction) {
			return new PyResponseEndClosure(pyFunction);
		}
		
		@Override
		public PyDictionary getHeaders() {
			return (PyDictionary) headers;
		}
		
	}
	
	static class PyResponseClosure implements ResponseClosure<PyFunction> {
		
		PyFunction pyFunction;
		
		public PyResponseClosure(PyFunction pyFunction) {
			this.pyFunction = pyFunction;
		}

		@Override
		public void call(String chunck) {
			pyFunction.__call__(new PyString(chunck));
		}
	}
	
	static class PyResponseEndClosure implements ResponseEndClosure<PyFunction> {

		PyFunction pyFunction;
		
		public PyResponseEndClosure(PyFunction pyFunction) {
			this.pyFunction = pyFunction;
		}

		@Override
		public void call() {
			pyFunction.__call__();
		}
	}
}
