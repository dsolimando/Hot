package be.solidx.hot;

import java.io.Writer;
import java.util.Map;

import be.solidx.hot.exceptions.ScriptException;

public interface ScriptExecutor<COMPILED_SCRIPT> {
	Object execute (Script<COMPILED_SCRIPT> script) throws ScriptException;
	Object execute (Script<COMPILED_SCRIPT> script, Map<String, Object> contextVars) throws ScriptException;
	Object execute (Script<COMPILED_SCRIPT> script, Writer writer) throws ScriptException;
	Object execute (Script<COMPILED_SCRIPT> script, Map<String, Object> contextVars, Writer writer) throws ScriptException;
	void compile (Script<COMPILED_SCRIPT> script) throws ScriptException;
}
