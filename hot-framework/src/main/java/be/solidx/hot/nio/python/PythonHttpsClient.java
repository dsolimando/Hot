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

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.nio.HttpsClient;
import be.solidx.hot.nio.python.PythonHttpClient.PyRequestCanceledClosure;
import be.solidx.hot.nio.python.PythonHttpClient.PyRequestClosure;
import be.solidx.hot.nio.python.PythonHttpClient.PyRequestErrorClosure;
import be.solidx.hot.nio.python.PythonHttpClient.PyResponse;

public class PythonHttpsClient extends HttpsClient<PyFunction,PyDictionary> {

	public PythonHttpsClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(eventLoopPool, defaultConversionService);
	}

	public PythonHttpsClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
	}
	
	public class PyHttpsRequest extends HttpsRequest {

		public PyHttpsRequest(Map<String, Object> options, PyFunction requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected RequestClosure<PyFunction, PyDictionary> buildRequestClosure(PyFunction requestClosure) {
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

	@Override
	protected Request buildRequest(PyDictionary options, PyFunction requestClosure) {
		return new PyHttpsRequest(options, requestClosure);
	}

	@Override
	protected Response<PyFunction,PyDictionary> buildResponse() {
		return new PyResponse();
	}
}
