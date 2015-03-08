package be.solidx.hot.nio.groovy;

import groovy.lang.Closure;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.nio.HttpsClient;
import be.solidx.hot.nio.groovy.GroovyHttpClient.GroovyRequestCanceledClosure;
import be.solidx.hot.nio.groovy.GroovyHttpClient.GroovyRequestClosure;
import be.solidx.hot.nio.groovy.GroovyHttpClient.GroovyRequestErrorClosure;
import be.solidx.hot.nio.groovy.GroovyHttpClient.GroovyResponse;

public class GroovyHttpsClient extends HttpsClient<Closure<?>,Map<String, Object>> {

	public GroovyHttpsClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(eventLoopPool, defaultConversionService);
	}

	public GroovyHttpsClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
	}
	
	@Override
	protected Request buildRequest(Map<String, Object> options, Closure<?> requestClosure) {
		return new GroovyHttpsRequest(options, requestClosure);
	}
	
	private class GroovyHttpsRequest extends HttpsRequest {
		
		public GroovyHttpsRequest(Map<String, Object> options, Closure<?> requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected RequestClosure<Closure<?>,Map<String, Object>> buildRequestClosure(Closure<?> requestClosure) {
			return new GroovyRequestClosure(requestClosure);
		}

		@Override
		protected RequestErrorClosure<Closure<?>> buildRequestErrorClosure(Closure<?> requestErrorClosure) {
			return new GroovyRequestErrorClosure(requestErrorClosure);
		}

		@Override
		protected RequestCanceledClosure<Closure<?>> buildRequestCanceledClosure(Closure<?> requestCanceledClosure) {
			return new GroovyRequestCanceledClosure(requestCanceledClosure);
		}
	}

	@Override
	protected Response<Closure<?>,Map<String, Object>> buildResponse() {
		return new GroovyResponse();
	}
}
