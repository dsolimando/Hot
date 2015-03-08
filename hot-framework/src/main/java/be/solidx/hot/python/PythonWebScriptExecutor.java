package be.solidx.hot.python;

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
