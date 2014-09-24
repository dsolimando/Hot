package be.icode.hot.web.deprecated;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class HotControllerImpl {
	
	private static final Log logger = LogFactory.getLog(HotControllerImpl.class);
	
	protected boolean devMode = true;
	
	protected static final HttpHeaders httpHeaders = new HttpHeaders();
	static {
		httpHeaders.put("Content-Type", Arrays.asList("text/html; charset=utf-8"));
	}

	protected InputStream loadResource(String path) throws IOException {
		if (devMode) {
			return be.icode.hot.utils.IOUtils.loadResourceNoCache(path);
		} else {
			return getClass().getClassLoader().getResourceAsStream(path);
		}
	}
	
	protected void printErrorPage (Exception e, Writer writer) {
		try {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			String[] splited = IOUtils.toString(loadResource("pages/error.hotg")).split("errorMessage");
			writer.write(splited[0] + stringWriter.toString() + splited [1]);
		} catch (IOException e1) {
			logger.error("",e1);
		}
	}
	
	protected ResponseEntity<String> printErrorPage (String errormessage) {
		try {
			String[] splited = IOUtils.toString(loadResource("pages/error.hotg")).split("errorMessage");
			return new ResponseEntity<String>(splited[0] + errormessage + splited [1],HttpStatus.OK);
		} catch (IOException e) {
			logger.error("",e);
			throw new RuntimeException(e);
		}
	}
	
	protected String extractStackTrace (Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return printWriter.toString();
	}
	
	protected String errorReport (Exception e) {
		try {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			String[] splited = IOUtils.toString(loadResource("pages/error.hotg")).split("errorMessage");
			return splited[0] + stringWriter.toString() + splited [1];
		} catch (IOException e1) {
			logger.error("",e1);
			return null;
		}
	}
	
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
}
