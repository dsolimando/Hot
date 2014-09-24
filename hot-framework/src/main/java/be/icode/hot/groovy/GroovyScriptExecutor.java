package be.icode.hot.groovy;

import java.util.List;

import be.icode.hot.JSR223ScriptExecutor;

public class GroovyScriptExecutor extends JSR223ScriptExecutor {

	public GroovyScriptExecutor() {
		super("groovy");
	}

	public GroovyScriptExecutor(List<String> globalScriptsPaths) {
		super("groovy", globalScriptsPaths);
	}
}
