package be.icode.hot.nio.javascript;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.springframework.core.convert.support.DefaultConversionService;

import be.icode.hot.nio.HttpsClient;
import be.icode.hot.nio.javascript.JsHttpClient.JsRequestCanceledClosure;
import be.icode.hot.nio.javascript.JsHttpClient.JsRequestClosure;
import be.icode.hot.nio.javascript.JsHttpClient.JsRequestErrorClosure;
import be.icode.hot.nio.javascript.JsHttpClient.JsResponse;

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
