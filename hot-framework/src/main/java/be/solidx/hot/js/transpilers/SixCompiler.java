package be.solidx.hot.js.transpilers;

import be.solidx.hot.Script;

public class SixCompiler extends JsTranspiler {

	public SixCompiler() {
		compiler = new Script<org.mozilla.javascript.Script>("Six.compile(sourceCode,{global:true})\n".getBytes(), "six-transpiler");
	}
}
