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

import be.solidx.hot.utils.HttpDataDeserializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class RestRequest<T extends Map<?, ?>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestRequest.class);
	
	T pathParams;
	
	Principal principal;
	
	T headers;
	
	T requestParams;

	T user;
	
	Session session;
	
	Object requestBody;
	
	String ip;
	
	HttpDataDeserializer httpDatadeSerializer;
	
	byte[] body;
	
	public RestRequest() {}
	
	public T getPathParams() {
		return pathParams;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public T getHeaders() {
		return headers;
	}

	public T getRequestParams() {
		return requestParams;
	}

	public Object getRequestBody() {
		return requestBody;
	}

	public Object getBody() {
	    return requestBody;
	}
	
	public T getUser() {
	    return user;
    }
	
	public Session getSession() {
		return session;
	}
	
	public String getIp() {
		return ip;
	}

	public static class Principal {
		
		String name;

		public Principal(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}

	public static class Session {
		
		private static final String ID = "id";
		private static final String CREATION_TIME = "creation-time";
		private static final String LAST_ACCESS_TIME = "last-access-time";
		private static final String MAX_INTERVAL = "max-interval";
		
		HttpSession servletSession;
		
		public Session(HttpSession servletSession) {
			this.servletSession = servletSession;
		}
		
		public Object attribute (String name) {
			switch (name) {
			case ID:
				return servletSession.getId();
			case CREATION_TIME:
				return servletSession.getCreationTime();
			case LAST_ACCESS_TIME:
				return servletSession.getLastAccessedTime();
			case MAX_INTERVAL:
				return servletSession.getMaxInactiveInterval();

			default:
				return servletSession.getAttribute(name);
			}
		}

		public Session invalidate() {
		    servletSession.invalidate();
		    return this;
        }

		public void setDuration(int seconds) {
            servletSession.setMaxInactiveInterval(seconds);
        }
		
		public Object attr(String name) {
			return attribute(name);
		}
		
		public Session attribute (String name, Object value) {
			if (value == null) {
				servletSession.removeAttribute(name);
			} else if (!Arrays.asList(ID,CREATION_TIME, LAST_ACCESS_TIME, MAX_INTERVAL).contains(name)) {
				servletSession.setAttribute(name, value);
			} 
			return this;
		}
		
		public Session attr(String name, Object value) {
			attribute(name, value);
			return this;
		}
	}

	public static class Part {

	    private String name;
	    private String value;

        public Part(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isFile() {
            return false;
        }
    }

    public static class FilePart extends Part {

	    private String contentType;

	    private long size;

	    private boolean inMemory;

	    private byte[] fileContent;

	    private String filename;

	    private InputStream inputStream;

        public FilePart(String name, String contentType, long size, byte[] fileContent) {
            super(name, null);
            this.contentType = contentType;
            this.size = size;
            this.fileContent = fileContent;
            inMemory = true;
        }

        public FilePart(String name, String filename, String contentType, long size, InputStream inputStream) {
            super(name, null);
            this.contentType = contentType;
            this.size = size;
            this.filename = filename;
            inMemory = false;
            this.inputStream = inputStream;
        }

        public void mv (String destinationPath) throws IOException {
            IOUtils.copy(inputStream,new FileOutputStream(destinationPath));
        }

        public String getContentType() {
            return contentType;
        }

        public long getSize() {
            return size;
        }

        public boolean isInMemory() {
            return inMemory;
        }

        public byte[] getFileContent() {
            return fileContent;
        }

        public String getFilename() {
            return filename;
        }

        public boolean isFile() {
            return true;
        }
    }
}
