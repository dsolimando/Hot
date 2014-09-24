package be.icode.hot.js.transpilers;

import be.icode.hot.Script;

public class SixCompiler extends JsTranspiler {

	public SixCompiler() {
		compiler = new Script<org.mozilla.javascript.Script>("Six.compile(sourceCode,{global:true})\n".getBytes(), "six-transpiler");
	}
}
