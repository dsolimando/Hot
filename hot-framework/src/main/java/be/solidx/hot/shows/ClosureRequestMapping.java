package be.solidx.hot.shows;

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

import be.solidx.hot.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ClosureRequestMapping {

	List<String> paths = new ArrayList<String>();
	
	RequestMethod requestMethod;
	
	List<String> headers = new ArrayList<String>();
	
	List<String> params = new ArrayList<String>();
	
	Options options = new Options();
	
	Closure closure;
	
	boolean auth = false;
	
	String[] roles;
	
	boolean anonymous;
	
	ExecutorService eventLoop;

	int scale = -1;

	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public Closure getClosure() {
		return closure;
	}

	public void setClosure(Closure closure) {
		this.closure = closure;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}
	
	
	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}
	
	public RequestMethod getRequestMethod() {
		return requestMethod;
	}
	
	public ExecutorService getEventLoop() {
		return eventLoop;
	}

	public void setEventLoop(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
	}
	

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    @Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClosureRequestMapping) {
			ClosureRequestMapping closureRequestMapping = (ClosureRequestMapping) obj;
			return CollectionUtils.isEqualCollection(getPaths(), closureRequestMapping.getPaths())
					&& getRequestMethod().equals(closureRequestMapping.getRequestMethod())
					&& CollectionUtils.isEqualCollection(getHeaders(), closureRequestMapping.getHeaders());
		}
		return false;
	}
	
	public static class Options {
		
		public static final String REST_OPTIONS_PROCESS_REQUEST_DATA = "processRequestData";
		public static final String REST_OPTIONS_PROCESS_RESPONSE_DATA = "processResponseData";
		
		protected boolean processRequestData = true;
		protected boolean processResponseData = true;
		
		public boolean isProcessRequestData() {
			return processRequestData;
		}
		public void setProcessRequestData(boolean processRequestData) {
			this.processRequestData = processRequestData;
		}
		public boolean isProcessResponseData() {
			return processResponseData;
		}
		public void setProcessResponseData(boolean processResponseData) {
			this.processResponseData = processResponseData;
		}
	}
}
