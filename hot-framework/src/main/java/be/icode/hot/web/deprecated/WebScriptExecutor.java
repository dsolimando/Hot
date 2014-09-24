package be.icode.hot.web.deprecated;

import java.io.Writer;
import java.util.Map;

import org.springframework.web.context.request.WebRequest;

import be.icode.hot.Script;
import be.icode.hot.ScriptExecutor;
import be.icode.hot.exceptions.ScriptException;

public interface WebScriptExecutor<COMPILED_SCRIPT,DB> extends ScriptExecutor<COMPILED_SCRIPT> {

	Object execute(Script<COMPILED_SCRIPT> script, WebRequest webRequest, Map<String, DB> dbMap) throws ScriptException;

	Object execute(Script<COMPILED_SCRIPT> script, WebRequest webRequest, Map<String, DB> dbMap, Writer writer) throws ScriptException;
}