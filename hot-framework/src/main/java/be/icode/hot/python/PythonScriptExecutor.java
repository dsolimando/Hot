package be.icode.hot.python;

import java.util.List;

import be.icode.hot.JSR223ScriptExecutor;

public class PythonScriptExecutor extends JSR223ScriptExecutor {

	public PythonScriptExecutor() {
		super("python");
	}

	public PythonScriptExecutor(List<String> globalScriptsPaths) {
		super("python", globalScriptsPaths);
	}
}
