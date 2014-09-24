package be.icode.hot.js.transpilers;

import be.icode.hot.Script;

public class CoffeeScriptCompiler extends JsTranspiler {
	
	public CoffeeScriptCompiler() {
		compiler = new Script<org.mozilla.javascript.Script>("CoffeeScript.compile(sourceCode, {})".getBytes(), "coffiescript-compler");
	}
}
