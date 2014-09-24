package be.icode.hot.groovy;

import be.icode.hot.HotPageCompiler;

public class GroovyPageCompiler extends HotPageCompiler{

	@Override
	protected String print(String toPrint) {
		return "println '''"+ toPrint.replaceAll("\\\\", "\\\\\\\\") +"'''\n";
	}

}
