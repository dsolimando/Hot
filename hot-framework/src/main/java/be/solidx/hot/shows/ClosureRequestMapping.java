package be.solidx.hot.shows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import be.solidx.hot.Closure;

public class ClosureRequestMapping {

	List<String> paths = new ArrayList<String>();
	
	RequestMethod requestMethod;
	
	List<String> headers = new ArrayList<String>();
	
	List<String> params = new ArrayList<String>();
	
	boolean sync;
	
	Options options = new Options();
	
	Closure closure;
	
	boolean auth = false;
	
	String[] roles;
	
	boolean anonymous;
	
	ExecutorService eventLoop;
	
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

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
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
