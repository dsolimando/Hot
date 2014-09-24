package be.icode.hot.web.deprecated;

import java.io.Writer;
import java.util.Map;

import javax.script.CompiledScript;

import org.python.core.PyDictionary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import be.icode.hot.HotPageCompiler;
import be.icode.hot.Script;
import be.icode.hot.data.DB;

@Controller
public class PythonController extends ScriptExecutorController<CompiledScript, DB<PyDictionary>> {
	
	@Override
	@RequestMapping(value = "/{scriptName}.py",method = RequestMethod.GET)
	public ResponseEntity<String> handleScript(WebRequest webRequest, @PathVariable String scriptName) {
		return super.handleScript(webRequest, scriptName);
	}
	
	@Override
	@RequestMapping(value = "/{page}.hotp", method = RequestMethod.GET)
	public ResponseEntity<String> printHotPage(WebRequest webRequest, @PathVariable String page) {
		return super.printHotPage(webRequest, page);
	}
	
	@Override
	@RequestMapping(value = "/{script}.py",method = RequestMethod.POST)
	public String handleScriptPOST (WebRequest webRequest, @PathVariable String script, Writer writer) {
		return super.handleScriptPOST(webRequest, script, writer);
	}
	
	@Override
	protected String getScriptExtension() {
		return ".py";
	}

	@Override
	protected String getPageExtension() {
		return ".hotp";
	}
	
	public void setDbMap (Map<String, DB<PyDictionary>> dbMap) {
		this.dbMap = dbMap;
	}
	
	public void setPageCompiler(HotPageCompiler pageCompiler) {
		this.pageCompiler = pageCompiler;
	}
	
	public void setScriptExecutor(WebScriptExecutor<CompiledScript, DB<PyDictionary>> scriptExecutor) {
		this.scriptExecutor = scriptExecutor;
	}

	@Override
	protected Script<CompiledScript> buildScript(byte[] code, String name) {
		return new Script<CompiledScript>(code, name);
	}
}
