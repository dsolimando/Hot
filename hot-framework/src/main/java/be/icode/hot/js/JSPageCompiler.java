package be.icode.hot.js;

import be.icode.hot.HotPageCompiler;

public class JSPageCompiler extends HotPageCompiler {

	@Override
	protected String print(String toPrint) {
		return "hprint ('"+ toPrint.replaceAll("\n", "\t") +"');\n";
	}

}
