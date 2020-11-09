package be.solidx.hot.nio.groovy;

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
