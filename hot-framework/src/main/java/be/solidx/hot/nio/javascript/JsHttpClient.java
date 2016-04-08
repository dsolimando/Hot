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
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.nio.HttpClient;

public class JsHttpClient extends HttpClient<NativeFunction,NativeObject> {
	
	Scriptable globalScope;
	
	public JsHttpClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService, Scriptable globalScope) {
		super(eventLoopPool, defaultConversionService);
		this.globalScope = globalScope;
	}
	
	public JsHttpClient(
			ExecutorService bossExecutorService, 
			ExecutorService eventLoopPool, 
			DefaultConversionService defaultConversionService, 
			Scriptable globalScope) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
		this.globalScope = globalScope;
	}

	@Override
	protected Request buildRequest(NativeObject options, NativeFunction requestClosure) {
		return new JsRequest(options, requestClosure);
	}

	@Override
	protected Response<NativeFunction,NativeObject> buildResponse() {
		return new JsResponse(globalScope);
	}

	public class JsRequest extends Request {

		public JsRequest(Map<String, Object> options, NativeFunction requestClosure) {
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
	
	static class JsRequestClosure implements RequestClosure<NativeFunction,NativeObject> {

		NativeFunction requestClosure;
		
		Scriptable globalScope;
		
		public JsRequestClosure(Scriptable globalScope, NativeFunction requestClosure) {
			this.requestClosure = requestClosure;
			this.globalScope = globalScope;
		}

		@Override
		public void call(Response<NativeFunction,NativeObject> response) {
			Context context = Context.enter();
			requestClosure.call(context, globalScope, requestClosure, new Object[]{response});
			Context.exit();
		}
	}
	
	static class JsRequestErrorClosure implements RequestErrorClosure<NativeFunction> {
		NativeFunction requestErrorClosure;
		
		Scriptable globalScope;

		public JsRequestErrorClosure(Scriptable globalScope, NativeFunction requestErrorClosure) {
			this.requestErrorClosure = requestErrorClosure;
			this.globalScope = globalScope;
		}
		
		@Override
		public void call(String message) {
			Context context = Context.enter();
			requestErrorClosure.call(context, globalScope, requestErrorClosure, new Object[]{message});
			Context.exit();
		}
	}
	
	static class JsRequestCanceledClosure implements RequestCanceledClosure<NativeFunction> {
		NativeFunction requestCanceledClosure;
		
		Scriptable globalScope;

		public JsRequestCanceledClosure(Scriptable globalScope, NativeFunction requestCanceledClosure) {
			this.requestCanceledClosure = requestCanceledClosure;
			this.globalScope = globalScope;
		}
		
		@Override
		public void call(String message) {
			Context context = Context.enter();
			requestCanceledClosure.call(context, globalScope, requestCanceledClosure, new Object[]{message});
			Context.exit();
		}
	}
	
	public static class JsResponse extends Response<NativeFunction,NativeObject> {
		
		Scriptable globalScope;
		
		public JsResponse(Scriptable globalScope) {
			this.globalScope = globalScope;
		}

		@Override
		protected ResponseClosure<NativeFunction> buildResponseClosure(NativeFunction closure) {
			return new JsResponseClosure(globalScope, closure);
		}

		@Override
		protected ResponseEndClosure<NativeFunction> buildResponseEndClosure(NativeFunction closure) {
			return new JsResponseEndClosure(globalScope, closure);
		}
		
		@Override
		public NativeObject getHeaders() {
			NativeObject nativeObject = new NativeObject();
			for (Entry<String, Object> entry : headers.entrySet()) {
				nativeObject.put(entry.getKey(), entry.getValue());
			}
			return nativeObject;
		}
	}
	
	static class JsResponseClosure implements ResponseClosure<NativeFunction> {

		NativeFunction responseClosure;
		
		Scriptable globalScope;
		
		public JsResponseClosure(Scriptable globalScope, NativeFunction responseClosure) {
			this.responseClosure = responseClosure;
			this.globalScope = globalScope;
		}
		
		@Override
		public void call(String chunck) {
			Context context = Context.enter();
			responseClosure.call(context, globalScope, responseClosure, new Object[]{chunck});
			Context.exit();
		}
	}
	
	static class JsResponseEndClosure implements ResponseEndClosure<NativeFunction> {
		
		Scriptable globalScope;
		
		NativeFunction responseEndClosure;
		
		public JsResponseEndClosure(Scriptable globalScope, NativeFunction responseEndClosure) {
			this.responseEndClosure = responseEndClosure;
			this.globalScope = globalScope;
		}

		@Override
		public void call() {
			Context context = Context.enter();
			responseEndClosure.call(context, globalScope, responseEndClosure, new Object[]{});
			Context.exit();
		}
	}
}
