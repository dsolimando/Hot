package be.solidx.hot.nio.javascript;

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

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.nio.HttpsClient;
import be.solidx.hot.nio.javascript.JsHttpClient.JsRequestCanceledClosure;
import be.solidx.hot.nio.javascript.JsHttpClient.JsRequestClosure;
import be.solidx.hot.nio.javascript.JsHttpClient.JsRequestErrorClosure;
import be.solidx.hot.nio.javascript.JsHttpClient.JsResponse;

public class JshttpsClient extends HttpsClient<NativeFunction,NativeObject> {
	
	Scriptable globalScope;

	public JshttpsClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService, Scriptable globalScope) {
		super(eventLoopPool, defaultConversionService);
		this.globalScope = globalScope;
	}

	public JshttpsClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService, Scriptable globalScope) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
		this.globalScope = globalScope;
	}

	@Override
	protected Request buildRequest(NativeObject options, NativeFunction requestClosure) {
		return new HttpsJsRequest(options, requestClosure);
	}

	@Override
	protected Response<NativeFunction,NativeObject> buildResponse() {
		return new JsResponse(globalScope);
	}

	public class HttpsJsRequest extends HttpsRequest {

		public HttpsJsRequest(Map<String, Object> options, NativeFunction requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected RequestClosure<NativeFunction,NativeObject> buildRequestClosure(NativeFunction requestClosure) {
			return new JsRequestClosure(globalScope, requestClosure);
		}

		@Override
		protected RequestErrorClosure<NativeFunction> buildRequestErrorClosure(NativeFunction requestErrorClosure) {
			return new JsRequestErrorClosure(globalScope, requestErrorClosure);
		}

		@Override
		protected RequestCanceledClosure<NativeFunction> buildRequestCanceledClosure(NativeFunction requestCanceledClosure) {
			return new JsRequestCanceledClosure(globalScope, requestCanceledClosure);
		}
	}
}
