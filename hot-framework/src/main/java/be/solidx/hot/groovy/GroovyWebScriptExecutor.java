package be.solidx.hot.groovy;

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

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.Script;
import be.solidx.hot.data.DB;
import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.web.deprecated.WebScriptExecutor;

public class GroovyWebScriptExecutor extends GroovyScriptExecutor implements WebScriptExecutor<CompiledScript,DB<Map<String, Object>>> {
	
	protected GroovyMapConverter groovyDataConverter;

	public GroovyWebScriptExecutor() {
	}

	public GroovyWebScriptExecutor(List<String> globalScriptsPaths) {
		super(globalScriptsPaths);
	}

	@Override
	public Object execute(Script<CompiledScript> script, WebRequest webRequest, Map<String,DB<Map<String, Object>>> dbMap) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap));
	}
	
	@Override
	public Object execute(Script<CompiledScript> script, WebRequest webRequest, Map<String, DB<Map<String, Object>>> dbMap, Writer writer) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap), writer);
	}
	
	protected Map<String, Object> createHotGLobalObject(WebRequest webRequest, Map<String, DB<Map<String, Object>>> dbMap) {
		Map<String, Object> root = new HashMap<String, Object>();
		Map<String, Object> hot = new HashMap<String, Object>();
		Map<String, Object> web = new HashMap<String, Object>();
		hot.put("db", dbMap);
		hot.put("web", web);
		web.put("request", webRequest);
		web.put("GET", groovyDataConverter.toMap((webRequest.getParameterMap())));
		hot.put("logger", LOGGER);
		root.put("hot", hot);
		return root;
	}
	
	public void setGroovyDataConverter(GroovyMapConverter groovyDataConverter) {
		this.groovyDataConverter = groovyDataConverter;
	}
}
