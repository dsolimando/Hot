package be.icode.hot.js;

import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JSLoader {
	
	private ScriptEngine scriptEngine;

	private String rootPath;

	public JSLoader(ScriptEngine scriptEngine, String rootPath) {
		this.scriptEngine = scriptEngine;
		this.rootPath = rootPath;
	}
	
	public JSLoader(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
		this.rootPath = "";
	}

	public void load(String path) throws ScriptException {
		scriptEngine.eval(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(rootPath + "/" + path)));
	}
}
