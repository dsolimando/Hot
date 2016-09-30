package be.solidx.hot;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.solidx.hot.exceptions.ScriptException;

public abstract class JSR223ScriptExecutor implements ScriptExecutor<CompiledScript> {
	
	protected static final Log LOGGER = LogFactory.getLog(JSR223ScriptExecutor.class);
	
	private ScriptEngine scriptEngine;
	
	private ScriptEngineManager manager = new ScriptEngineManager();
	
	protected boolean devMode = false;
	
	protected String engineName;
	
	protected Map<String, Script<CompiledScript>> compilableMap = new HashMap<String, Script<CompiledScript>>();
	
	protected List<Script<CompiledScript>> preExecuteScriptMap = new LinkedList<Script<CompiledScript>>();
	
	public JSR223ScriptExecutor(String engineName) {
		this.engineName = engineName;
	}
	
	public JSR223ScriptExecutor(String engineName, List<String> globalScriptsPaths) {
		this(engineName);
		compileGlobalScopeScripts(globalScriptsPaths);
	}
	
	private ScriptEngine getEngine () {
		// In dev mode we create new engine for recompiling sources
		if (devMode || scriptEngine == null) {
			scriptEngine = manager.getEngineByName(engineName);
		}
		return scriptEngine;
	}
	
	@Override
	public Object execute(Script<CompiledScript> script) throws ScriptException {
		try {
			ScriptEngine scriptEngine = getEngine();
			CompiledScript compiledScript = getCachedScript(script);
			ScriptContext scriptContext = new SimpleScriptContext();
			executePreExecuteScripts(scriptContext);
			Object object = compiledScript.eval(scriptContext);
			if (object == null) return scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			return object;
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(script.getName(), e);
		}
	}

	@Override
	public Object execute(Script<CompiledScript> script, Map<String, Object> contextVars) throws ScriptException {
		try {
			ScriptEngine scriptEngine = getEngine();
			Bindings bindings = scriptEngine.createBindings();
			ScriptContext scriptContext = new SimpleScriptContext();
			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			bindings.putAll(contextVars);
			executePreExecuteScripts(scriptContext);
			CompiledScript compiledScript = getCachedScript(script);
			Object object = compiledScript.eval(scriptContext);
			if (object == null) return bindings;
			return object;
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(script.getName(),e);
		}
	}

	@Override
	public Object execute(Script<CompiledScript> script, Writer writer) throws ScriptException {
		try {
			ScriptEngine scriptEngine = getEngine();
			CompiledScript compiledScript = getCachedScript(script);
			SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
			executePreExecuteScripts(simpleScriptContext);
			simpleScriptContext.setWriter(writer);
			Object object = compiledScript.eval(simpleScriptContext);
			writer.flush();
			if (object == null) return scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			return object;
		} catch (Exception e) {
			throw new ScriptException(script.getName(), e);
		}
	}

	@Override
	public Object execute(Script<CompiledScript> script, Map<String, Object> contextVars, Writer writer) throws ScriptException {
		try {
			ScriptEngine scriptEngine = getEngine();
			SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
			Bindings bindings = scriptEngine.createBindings();
			bindings.putAll(contextVars);
			simpleScriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			executePreExecuteScripts(simpleScriptContext);
			simpleScriptContext.setWriter(writer);
			CompiledScript compiledScript = getCachedScript(script);
			Object object = compiledScript.eval(simpleScriptContext);
			writer.flush();
			if (object == null) return bindings;
			return object;
		} catch (Exception e) {
			throw new ScriptException(script.getName(),e);
		} 
	}

	@Override
	public void compile(Script<CompiledScript> script) throws ScriptException {
		try {
			System.setProperty("groovy.source.encoding", "UTF-8");
			CompiledScript compiledScript = ((Compilable)getEngine()).compile(script.getCodeUTF8());
			script.setCompiledScript(compiledScript);
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(script.getName(),e);
		}
	}
	
	private void compileGlobalScopeScripts(List<String> globalScriptsPaths) {
		System.setProperty("groovy.source.encoding", "UTF-8");
		for (String path : globalScriptsPaths) {
			try {
				String correctedPath = path.startsWith("/")?path:path+"/";
				Script<CompiledScript> script = new Script<CompiledScript>(IOUtils.toByteArray(getClass().getResourceAsStream(correctedPath)),correctedPath);
				compile(script);
				preExecuteScriptMap.add(script);
			} catch (Exception e) {
				LOGGER.error("Failed to compile script "+path,e);
			} 
		}
	}
	
	private void executePreExecuteScripts (ScriptContext scriptContext) {
		for (Script<CompiledScript> compiledScript : preExecuteScriptMap) {
			try {
				compiledScript.getCompiledScript().eval(scriptContext);
			} catch (javax.script.ScriptException e) {
				LOGGER.error("Failed to execute script "+compiledScript.getName(),e);
			}
		}
	}

	private CompiledScript getCachedScript (Script<CompiledScript> script) throws ScriptException {
		Script<CompiledScript> compiledScript = compilableMap.get(script.getMd5());
		if ((compiledScript != null && !devMode) || (compiledScript != null && devMode &&  !script.outdated(compiledScript.getCodeUTF8()))) {
			return compiledScript.getCompiledScript();
		} else {
			synchronized (scriptEngine) {
				compile(script);
				compilableMap.put(script.getMd5(), script);
				if (compiledScript != null && script.outdated(compiledScript.getCodeUTF8())) {
					compilableMap.remove(compiledScript.getMd5());
				}
			}
			return script.getCompiledScript();
		}
	}
	
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
}
