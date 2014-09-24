package hot;

import java.util.HashMap;
import java.util.Map;


public class Response {

	private int status = 200;
	
	private Map<?, ?> headers;
	
	private Object body;

	public Response(int status, Map<?, ?> headers, Object body) {
		this.status = status;
		this.headers = headers;
		this.body = body;
	}
	
	public Response(int status) {
		this.status = status;
		this.headers = new HashMap<Object, Object>();
		this.body = "";
	}
	
	public Response(int status, Object body) {
		this.status = status;
		this.headers = new HashMap<Object, Object>();
		this.body = body;
	}

	public Response(Map<?, ?> headers, Object body) {
		this.headers = headers;
		this.body = body;
	}

	public int getStatus() {
		return status;
	}

	public Map<?, ?> getHeaders() {
		return headers;
	}

	public Object getBody() {
		return body;
	}
}
