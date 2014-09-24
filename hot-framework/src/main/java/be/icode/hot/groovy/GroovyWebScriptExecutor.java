package be.icode.hot.groovy;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import org.springframework.web.context.request.WebRequest;

import be.icode.hot.Script;
import be.icode.hot.data.DB;
import be.icode.hot.exceptions.ScriptException;
import be.icode.hot.web.deprecated.WebScriptExecutor;

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
