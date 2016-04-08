package be.solidx.hot.web.deprecated;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.HotPageCompiler;
import be.solidx.hot.Script;

public abstract class ScriptExecutorController<COMPILED_SCRIPT,DB> extends HotControllerImpl {
	
	protected static final Log LOGGER = LogFactory.getLog(ScriptExecutorController.class);
	
	protected WebScriptExecutor<COMPILED_SCRIPT,DB> scriptExecutor;
	
	protected Map<String, DB> dbMap;
	
	protected HotPageCompiler pageCompiler;
	
	public ResponseEntity<String> handleScript(WebRequest webRequest, String scriptName) {
		try {
			scriptName = scriptName + getScriptExtension();
			Script<COMPILED_SCRIPT> script = buildScript(IOUtils.toByteArray(loadResource(scriptName)), scriptName);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			scriptExecutor.execute(script, webRequest, dbMap, printWriter);
			return new ResponseEntity<String>(stringWriter.toString(), httpHeaders, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			printErrorPage(e, stringWriter);
			return new ResponseEntity<String>(stringWriter.toString(), httpHeaders, HttpStatus.ACCEPTED);
		}
	}
	
	public String handleScriptPOST (WebRequest webRequest, String scriptName, Writer writer) {
		try {
			scriptName = scriptName + getScriptExtension();
			Script<COMPILED_SCRIPT> script = buildScript(IOUtils.toByteArray(loadResource(scriptName)), scriptName);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			Object result = scriptExecutor.execute(script, webRequest, dbMap, printWriter);
			printWriter.flush();
			stringWriter.flush();
			if (result == null) throw new Exception("POST handling scripts must return a page URL to redirect to");
			return "redirect:/"+(String) result;
		} catch (Exception e) {
			printErrorPage(e, writer);
			return null;
		}
	}
	
	public ResponseEntity<String> printHotPage(WebRequest webRequest, String page) {
		ResponseEntity<String> responseEntity;
		try {
			page = page + getPageExtension();
			Script<String> pageScript = new Script<String>(IOUtils.toByteArray(loadResource(page)), page);
			byte[] bytes= pageCompiler.compile(pageScript).getBytes("UTF-8");
			Script<COMPILED_SCRIPT> script = buildScript(bytes, page);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			scriptExecutor.execute(script, webRequest, dbMap, printWriter);
			responseEntity = new ResponseEntity<String>(stringWriter.toString(), httpHeaders, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			printErrorPage(e, writer);
			responseEntity = new ResponseEntity<String>(writer.toString(), httpHeaders, HttpStatus.ACCEPTED);
		}
		return responseEntity;
	}
	
	protected abstract String getScriptExtension ();
	
	protected abstract String getPageExtension ();
	
	protected abstract Script<COMPILED_SCRIPT> buildScript (byte[] code, String name);
}
