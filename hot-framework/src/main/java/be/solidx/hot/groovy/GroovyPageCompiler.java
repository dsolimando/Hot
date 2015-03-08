package be.solidx.hot.groovy;

import be.solidx.hot.HotPageCompiler;

public class GroovyPageCompiler extends HotPageCompiler{

	@Override
	protected String print(String toPrint) {
		return "println '''"+ toPrint.replaceAll("\\\\", "\\\\\\\\") +"'''\n";
	}

}
