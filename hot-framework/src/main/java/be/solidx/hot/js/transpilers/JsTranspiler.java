package be.solidx.hot.js.transpilers;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.NativeObject;

import be.solidx.hot.Script;
import be.solidx.hot.js.JSScriptExecutor;

public abstract class JsTranspiler {

protected JSScriptExecutor jsScriptExecutor;
	
	protected boolean devMode = true;
	
	protected Map<String, Script<String>> compilableMap = new ConcurrentHashMap<String, Script<String>>();
	
	protected Script<org.mozilla.javascript.Script> compiler;
	
	public String compile (Script<String> script) {
		Script<String> compiledScript = compilableMap.get(script.getMd5());
		if ((compiledScript != null && !devMode) || (compiledScript != null && devMode &&  !script.outdated(compiledScript.getCodeUTF8()))) {
			return compiledScript.getCompiledScript();
		} else {
			synchronized (compiler) {
				Map<String, Object> contextParams = new HashMap<String, Object>();
				contextParams.put("sourceCode", script.getCodeUTF8());
				NativeObject global = new NativeObject();
				NativeObject.putProperty(global, "Object", new NativeObject());
				contextParams.put("global", global);
				String js = (String) jsScriptExecutor.execute(compiler, contextParams);
				script.setCompiledScript(js);
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
	
	public void setJsScriptExecutor(JSScriptExecutor jsScriptExecutor) {
		this.jsScriptExecutor = jsScriptExecutor;
	}
}
