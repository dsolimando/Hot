package be.icode.hot.js.transpilers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.NativeObject;

import be.icode.hot.Script;
import be.icode.hot.js.JSScriptExecutor;

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
