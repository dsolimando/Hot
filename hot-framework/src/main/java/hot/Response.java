package hot;

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
