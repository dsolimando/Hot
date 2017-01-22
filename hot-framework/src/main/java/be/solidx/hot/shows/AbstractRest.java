package be.solidx.hot.shows;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import be.solidx.hot.Closure;
import be.solidx.hot.shows.ClosureRequestMapping.Options;

public abstract class AbstractRest<CLOSURE>  implements Rest<CLOSURE>, RestConfig<CLOSURE, Map<?,?>> {
	
	private static final Log LOG = LogFactory.getLog(AbstractRest.class);
	
	ExecutorService eventLoop;
	
	public AbstractRest(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
	}

	protected List<ClosureRequestMapping> requestMappings = new ArrayList<ClosureRequestMapping>();

	@Override
	public List<ClosureRequestMapping> getRequestMappings() {
		return requestMappings;
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> put (List<String> paths) {
		return new RestAuthHeadersImpl(buildRequestMapping(paths, RequestMethod.PUT));
	}
	
	@Override
	public be.solidx.hot.shows.Rest.RestAuthHeaders<CLOSURE> put(String path) {
		return put(Arrays.asList(path));
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> get (List<String> paths) {
		return new RestAuthHeadersImpl(buildRequestMapping(paths, RequestMethod.GET));
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> get (List<String> paths, Map<?,?> options) {
		return new RestAuthHeadersImpl(buildRequestMapping(paths, options, RequestMethod.GET));
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> get(String path) {
		return get(Arrays.asList(path));
	}
	
	@Override
	public be.solidx.hot.shows.Rest.RestAuthHeaders<CLOSURE> get(String path, Map<?, ?> options) {
		return get(Arrays.asList(path), options);
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> delete (List<String> paths) {
		return new RestAuthHeadersImpl(buildRequestMapping(paths, RequestMethod.DELETE));
	}
	
	@Override
	public RestAuthHeaders<CLOSURE> post (List<String> paths) {
		return new RestAuthHeadersImpl(buildRequestMapping(paths, RequestMethod.POST));
	}
	
	@Override
	public be.solidx.hot.shows.Rest.RestAuthHeaders<CLOSURE> delete(String path) {
		return delete(Arrays.asList(path));
	}
	
	@Override
	public be.solidx.hot.shows.Rest.RestAuthHeaders<CLOSURE> post(String path) {
		return post(Arrays.asList(path));
	}
	
	private ClosureRequestMapping buildRequestMapping (List<String> paths, RequestMethod requestMethod) {
		ClosureRequestMapping requestMapping = new ClosureRequestMapping();
		requestMapping.setPaths(paths);
		requestMapping.setRequestMethod(requestMethod);
		requestMapping.setEventLoop(eventLoop);
		return requestMapping;
	}
	
	@SuppressWarnings("rawtypes")
	protected ClosureRequestMapping buildRequestMapping (List<String> paths, Map<?,?> optionsMap, RequestMethod requestMethod) {
		ClosureRequestMapping closureRequestMapping = buildRequestMapping(paths, requestMethod);
		closureRequestMapping.setEventLoop(eventLoop);
		
		for (Entry entry : optionsMap.entrySet()) {
			if (entry.getKey() instanceof String && entry.getKey().equals(Options.REST_OPTIONS_PROCESS_REQUEST_DATA)) {
				try {
					closureRequestMapping.getOptions().setProcessRequestData(Boolean.parseBoolean((String) entry.getValue()));
				} catch (Exception e) {
					LOG.error("Invalid option value for "+Options.REST_OPTIONS_PROCESS_REQUEST_DATA);
				}
			}
			if (entry.getKey() instanceof String && entry.getKey().equals(Options.REST_OPTIONS_PROCESS_RESPONSE_DATA)) {
				try {
					closureRequestMapping.getOptions().setProcessResponseData(Boolean.parseBoolean((String) entry.getValue()));
				} catch (Exception e) {
					LOG.error("Invalid option value for "+Options.REST_OPTIONS_PROCESS_RESPONSE_DATA);
				}
			}
		}
		return closureRequestMapping;
	}
	
	protected abstract Closure buildShowClosure(CLOSURE closure, boolean async);
	
	private class RestClosureImpl implements RestClosure<CLOSURE> {
		
		ClosureRequestMapping requestMapping;
		
		public RestClosureImpl(ClosureRequestMapping requestMapping) {
			this.requestMapping = requestMapping;
		}

		@Override
		public void then(CLOSURE closure) {
			requestMapping.closure = buildShowClosure(closure,false);
			if (!requestMappings.contains(requestMapping)) {
				requestMappings.add(requestMapping);
			}
		}
		
		@Override
		public void async(CLOSURE closure) {
            requestMapping.closure = buildShowClosure(closure,true);
            if (!requestMappings.contains(requestMapping)) {
                requestMappings.add(requestMapping);
            }
		}
	}

	private class RestAuthClosureImpl extends RestClosureImpl implements RestAuth<CLOSURE> {
		
		public RestAuthClosureImpl(ClosureRequestMapping requestMapping) {
			super(requestMapping);
		}

		@Override
		public be.solidx.hot.shows.Rest.RestClosure<CLOSURE> auth(String... roles) {
			super.requestMapping.setAuth(true);
			super.requestMapping.setRoles(roles);
			return new RestClosureImpl(requestMapping);
		}

		@Override
		public be.solidx.hot.shows.Rest.RestClosure<CLOSURE> anonymous() {
			super.requestMapping.setAnonymous(true);
			return new RestClosureImpl(requestMapping);
		}
	}
	
	private class RestHeadersClosureImpl extends RestClosureImpl implements RestHeaders<CLOSURE> {
		
		public RestHeadersClosureImpl(ClosureRequestMapping requestMapping) {
			super(requestMapping);
		}

		@Override
		public be.solidx.hot.shows.Rest.RestClosure<CLOSURE> headers(String[] headers) {
			super.requestMapping.getHeaders().addAll(Arrays.asList(headers));
			return new RestClosureImpl(requestMapping);
		}
		
		@Override
		public be.solidx.hot.shows.Rest.RestClosure<CLOSURE> headers(String header) {
			return headers(new String[]{header});
		}
	}
	
	private class RestAuthHeadersImpl extends RestClosureImpl implements RestAuthHeaders<CLOSURE> {

		RestAuth<CLOSURE> restAuthClosure;
		RestHeaders<CLOSURE> restHeadersClosure;
		
		public RestAuthHeadersImpl(ClosureRequestMapping requestMapping) {
			super(requestMapping);
			restAuthClosure = new RestAuthClosureImpl(requestMapping);
			restHeadersClosure = new RestHeadersClosureImpl(requestMapping);
		}

		@Override
		public be.solidx.hot.shows.Rest.RestHeaders<CLOSURE> auth(String...roles) {
			restAuthClosure.auth(roles);
			return new RestHeadersClosureImpl(requestMapping);
		}
		
		@Override
		public be.solidx.hot.shows.Rest.RestHeaders<CLOSURE> anonymous() {
			restAuthClosure.anonymous();
			return new RestHeadersClosureImpl(requestMapping);
		}

		@Override
		public be.solidx.hot.shows.Rest.RestAuth<CLOSURE> headers(String[] headers) {
			restHeadersClosure.headers(headers);
			return new RestAuthClosureImpl(requestMapping);
		}
		
		@Override
		public be.solidx.hot.shows.Rest.RestAuth<CLOSURE> headers(String header) {
			restHeadersClosure.headers(header);
			return new RestAuthClosureImpl(requestMapping);
		}
	}
}
