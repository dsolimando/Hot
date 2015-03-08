package be.solidx.hot.js;

import be.solidx.hot.HotPageCompiler;

public class JSPageCompiler extends HotPageCompiler {

	@Override
	protected String print(String toPrint) {
		return "hprint ('"+ toPrint.replaceAll("\n", "\t") +"');\n";
	}

}
