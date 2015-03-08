package be.solidx.hot.js.transpilers;

import be.solidx.hot.Script;

public class CoffeeScriptCompiler extends JsTranspiler {
	
	public CoffeeScriptCompiler() {
		compiler = new Script<org.mozilla.javascript.Script>("CoffeeScript.compile(sourceCode, {})".getBytes(), "coffiescript-compler");
	}
}
