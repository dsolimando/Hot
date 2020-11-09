package be.solidx.hot.js;

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

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.data.DB;
import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.web.deprecated.WebScriptExecutor;

public class JSWebScriptExecutor extends JSScriptExecutor implements WebScriptExecutor<Script,DB<NativeObject>> {

	protected JsMapConverter jsDataConverter;
	
	public JSWebScriptExecutor() {
		super();
	}

	@Override
	public Object execute(be.solidx.hot.Script<Script> script, WebRequest webRequest, Map<String, DB<NativeObject>> dbMap) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap));
	}

	@Override
	public Object execute(be.solidx.hot.Script<Script> script, WebRequest webRequest, Map<String, DB<NativeObject>> dbMap, Writer writer) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap), writer);
	}

	protected Map<String, Object> createHotGLobalObject(WebRequest webRequest, Map<String, DB<NativeObject>> dbMap) {
		NativeObject hot = new NativeObject();
		NativeObject web = new NativeObject();
		NativeObject dbmapNO = new NativeObject();
		for (Entry<String, DB<NativeObject>> entry : dbMap.entrySet()) {
			dbmapNO.put(entry.getKey(), dbmapNO, entry.getValue());
		}
		hot.put("db",  hot, dbmapNO);
		hot.put("web", hot, web);
		web.put("request", web, webRequest);
		web.put("GET", web, jsDataConverter.toScriptMap(webRequest.getParameterMap()));
		hot.put("logger",hot, LOGGER);
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("hot", hot);
		return root;
	}
	
	public void setJsDataConverter(JsMapConverter jsDataConverter) {
		this.jsDataConverter = jsDataConverter;
	}
}
