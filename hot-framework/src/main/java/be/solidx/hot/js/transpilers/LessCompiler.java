package be.solidx.hot.js.transpilers;

import be.solidx.hot.Script;

public class LessCompiler extends JsTranspiler {

	public LessCompiler() {
		String script = "" +
				"var result\n" +
				"var parser = new less.Parser();\n" +
				"parser.parse(sourceCode, function (e, root) {" +
				"	if (!e) {\n" +
				"		result = root.toCSS();" +
				"	}" +
				"});" +
				"result";
		compiler = new Script<org.mozilla.javascript.Script>(script.getBytes(), "less-compler");
	}
}
