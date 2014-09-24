package be.icode.hot.js;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.springframework.web.context.request.WebRequest;

import be.icode.hot.data.DB;
import be.icode.hot.exceptions.ScriptException;
import be.icode.hot.web.deprecated.WebScriptExecutor;

public class JSWebScriptExecutor extends JSScriptExecutor implements WebScriptExecutor<Script,DB<NativeObject>> {

	protected JsMapConverter jsDataConverter;
	
	public JSWebScriptExecutor() {
		super();
	}

	@Override
	public Object execute(be.icode.hot.Script<Script> script, WebRequest webRequest, Map<String, DB<NativeObject>> dbMap) throws ScriptException {
		return super.execute(script, createHotGLobalObject(webRequest, dbMap));
	}

	@Override
	public Object execute(be.icode.hot.Script<Script> script, WebRequest webRequest, Map<String, DB<NativeObject>> dbMap, Writer writer) throws ScriptException {
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
