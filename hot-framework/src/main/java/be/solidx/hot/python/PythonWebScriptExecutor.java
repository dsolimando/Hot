package be.solidx.hot.python;

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

import org.python.core.PyDictionary;
import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.Script;
import be.solidx.hot.data.DB;
import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.utils.ScriptMapConverter;
import be.solidx.hot.web.deprecated.WebScriptExecutor;

public class PythonWebScriptExecutor extends PythonScriptExecutor implements WebScriptExecutor<CompiledScript,DB<PyDictionary>> {

	protected ScriptMapConverter<PyDictionary> pyDataConverter;
	
	public PythonWebScriptExecutor() {
		super();
	}

	public PythonWebScriptExecutor(List<String> preExecuteScriptPaths) {
		super(preExecuteScriptPaths);
	}
	
	@Override
	public Object execute(Script<CompiledScript> script, WebRequest webRequest, Map<String, DB<PyDictionary>> dbMap) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap));
	}
	
	@Override
	public Object execute(Script<CompiledScript> script, WebRequest webRequest, Map<String, DB<PyDictionary>> dbMap, Writer writer) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap), writer);
	}
	
	protected Map<String, Object> createHotGLobalObject(WebRequest webRequest, Map<String, DB<PyDictionary>> dbMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("__hot__db__dbmap", dbMap);
		map.put("__hot__web__request", dbMap);
		map.put("__hot__web__get", pyDataConverter.toScriptMap(webRequest.getParameterMap()));
		map.put("__hot__logger", LOGGER);
		return map;
	}
	
	public void setPyDataConverter(ScriptMapConverter<PyDictionary> pyDataConverter) {
		this.pyDataConverter = pyDataConverter;
	}
}
