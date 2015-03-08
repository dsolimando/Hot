package be.solidx.hot.js;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import be.solidx.hot.Script;
import be.solidx.hot.ScriptExecutor;
import be.solidx.hot.exceptions.ScriptException;

public class JSScriptExecutor implements ScriptExecutor<org.mozilla.javascript.Script> {
	
	protected static final Log LOGGER = LogFactory.getLog(JSScriptExecutor.class);

	private Scriptable globalScope;
	
	protected boolean devMode = false;
	
	protected boolean interpretive = false;
	
	protected List<org.mozilla.javascript.Script> preExecuteScripts = new LinkedList<org.mozilla.javascript.Script>();
	
	protected Map<String, Script<org.mozilla.javascript.Script>> compilableMap = new HashMap<String, Script<org.mozilla.javascript.Script>>();
	
	protected String commonsjsModuleRoot = "classpath:/escripts/";
	
	@SuppressWarnings("deprecation")
	public JSScriptExecutor() {
		Context context = Context.enter();
		if (devMode) {
			Context.setCachingEnabled(false);
		}
		globalScope = context.initStandardObjects();
		Context.exit();
	}
	
	private void putAllContextVars (Map<String, Object> contextVars, Scriptable scope) {
		for (Object key : contextVars.keySet()) {
			if (key instanceof String)
				ScriptableObject.putProperty(scope, (String) key, contextVars.get(key));
			else
				LOGGER.error(String.format("key value %s is not a String", key));
		}
	}
	
	private void enrichWithSugar (Scriptable scope, Context context) {
		ScriptableObject.putProperty(scope, "loader", new RhinoJsLoader(context, scope));
		context.evaluateString(scope, "function hprint (toPrint) {out.print(toPrint);out.flush();}", "printer", 1, null);
		context.evaluateString(scope, "function hprintln (toPrint) {out.println(toPrint);out.flush();}", "printer", 1, null);
		context.evaluateString(scope, "function hload (file) {loader.load (file);}", "printer", 1, null);
		context.evaluateString(scope, "global=function(){return this}();", "printer", 1, null);
	}
	
	private void commonsjs (Scriptable scope, Context context) {
		try {
			URI moduleRootUri = commonsjsModuleRoot.startsWith("classpath:")?
					getClass().getResource(commonsjsModuleRoot.split(":")[1]).toURI():
						new URI(commonsjsModuleRoot);
			ModuleScriptProvider moduleScriptProvider = new SoftCachingModuleScriptProvider(new UrlModuleSourceProvider(Arrays.asList(moduleRootUri), null));
			RequireBuilder builder = new RequireBuilder();
			builder.setModuleScriptProvider(moduleScriptProvider);
			Require require = builder.createRequire(context, scope);
			require.install(scope);
		} catch (Exception e) {
			LOGGER.error("",e);
		}
	}
	
	@Override
	public Object execute(Script<org.mozilla.javascript.Script> script) throws ScriptException {
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_1_8);
		Scriptable scope = context.newObject(globalScope);
		PrintWriter printWriter = new PrintWriter(System.out);
		ScriptableObject.putProperty(scope, "out", printWriter);
		enrichWithSugar(scope, context);
		commonsjs(scope, context);
		executePreExecuteScripts(context, scope);
		org.mozilla.javascript.Script jsScript = getCachedScript(script);
		Object o = jsScript.exec(context, scope);
		//Object o = context.evaluateString(scope, script.getCodeUTF8(), script.getName(), 1, null);
		printWriter.flush();
		Context.exit();
		scope = null;
		return o;
	}

	@Override
	public Object execute(Script<org.mozilla.javascript.Script> script, Map<String, Object> contextVars) throws ScriptException {
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_1_8);
		Scriptable scope = context.newObject(globalScope);
		putAllContextVars(contextVars, scope);
		PrintWriter printWriter = new PrintWriter(System.out);
		ScriptableObject.putProperty(scope, "out", printWriter);
		enrichWithSugar(scope, context);
		commonsjs(scope, context);
		executePreExecuteScripts(context, scope);
		org.mozilla.javascript.Script jsScript = getCachedScript(script);
		Object o = jsScript.exec(context, scope);
		printWriter.flush();
		Context.exit();
		scope = null;
		return o;
	}

	@Override
	public Object execute(Script<org.mozilla.javascript.Script> script, Writer writer) throws ScriptException {
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_1_8);
		Scriptable scope = context.newObject(globalScope);
		PrintWriter printWriter = new PrintWriter(writer);
		ScriptableObject.putProperty(scope, "out", printWriter);
		enrichWithSugar(scope, context);
		commonsjs(scope, context);
		executePreExecuteScripts(context, scope);
		org.mozilla.javascript.Script jsScript = getCachedScript(script);
		Object o = jsScript.exec(context, scope);
		Context.exit();
		scope = null;
		return o;
	}
	
	@Override
	public Object execute(Script<org.mozilla.javascript.Script> script,Map<String, Object> contextVars, Writer writer) throws ScriptException {
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_1_8);
		Scriptable scope = context.newObject(globalScope);
		putAllContextVars(contextVars, scope);
		PrintWriter printWriter = new PrintWriter(writer);
		ScriptableObject.putProperty(scope, "out", printWriter);
		enrichWithSugar(scope, context);
		commonsjs(scope, context);
		executePreExecuteScripts(context, scope);
		org.mozilla.javascript.Script jsScript = getCachedScript(script);
		Object o = jsScript.exec(context, scope);
		Context.exit();
		scope = null;
		return o;
	}

	@Override
	public void compile(Script<org.mozilla.javascript.Script> script) {
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_1_8);
		if (interpretive) context.setOptimizationLevel(-1);
		try {
			org.mozilla.javascript.Script compiledScript = context.compileString(script.getCodeUTF8(), script.getName(), 1, null);
			script.setCompiledScript(compiledScript);
		} catch (ScriptException e) {
			throw new ScriptException("Failed to compile script",e);
		} finally {
			Context.exit();
		}
	}
	
	private org.mozilla.javascript.Script getCachedScript (Script<org.mozilla.javascript.Script> script) throws ScriptException {
		
		Script<org.mozilla.javascript.Script> compiledScript = compilableMap.get(script.getMd5());
		if ((compiledScript != null && !devMode) || (compiledScript != null && devMode &&  !script.outdated(compiledScript.getCodeUTF8()))) {
			return compiledScript.getCompiledScript();
		} else {
			synchronized (compilableMap) {
				compile(script);
				compilableMap.put(script.getMd5(), script);
				if (compiledScript != null && script.outdated(compiledScript.getCodeUTF8())) {
					compilableMap.remove(compiledScript.getMd5());
				}
			}
			return script.getCompiledScript();
		}
	}
	
	private void executePreExecuteScripts (Context context, Scriptable scope) {
		for (org.mozilla.javascript.Script script : preExecuteScripts) {
			script.exec(context, scope);
		}
	}
	
	public void setPreExecuteScripts(List<String> preExecuteScriptPaths) {
		for (String path : preExecuteScriptPaths) {
			try {
				Context context = Context.enter();
				if (interpretive) {
					context.setOptimizationLevel(-1);
				}
				context.setLanguageVersion(Context.VERSION_1_8);
				String correctedPath = path.startsWith("/")?path:path+"/";
				org.mozilla.javascript.Script script = context.compileString(IOUtils.toString(getClass().getResourceAsStream(correctedPath)), correctedPath, 1, null);
				preExecuteScripts.add(script);
			} catch (Exception e) {
				LOGGER.error("Failed to Load script "+path,e);
			} 
		}
	}
	
	public void setGlobalScopeScripts(List<String> globalScopeScriptPaths) {
		for (String path : globalScopeScriptPaths) {
			try {
				Context context = Context.enter();
				context.setLanguageVersion(Context.VERSION_1_8);
				if (interpretive) {
					context.setOptimizationLevel(-1);
				}
				String correctedPath = path.startsWith("/")?path:path+"/";
				context.evaluateString(globalScope, IOUtils.toString(getClass().getResourceAsStream(correctedPath)), correctedPath, 1, null);
			} catch (Exception e) {
				LOGGER.error("Failed to Load script "+path,e);
			} finally {
				Context.exit();
			}
		}
	}
	
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	public void setInterpretive(boolean interpretive) {
		this.interpretive = interpretive;
	}
	
	public void setCommonsjsModuleRoot(String commonsjsModuleRoot) {
		this.commonsjsModuleRoot = commonsjsModuleRoot;
	}
	
	public Scriptable getGlobalScope() {
		return globalScope;
	}
}
