package be.icode.hot.python;

import be.icode.hot.HotPageCompiler;

public class PythonPageCompiler extends HotPageCompiler{

	@Override
	protected String print(String toPrint) {
		return "print ('''"+ toPrint.trim() +"''');\n";
	}
	
	@Override
	protected String printScript(String toPrint) {
		toPrint = removeFirstBlankLine(toPrint).replaceAll("\t", "   ");
		int lead = countLeadingSpaces(toPrint);
		return toPrint.replaceAll("\n {"+lead+"}", "\n").trim()+"\n";
	}
	
	private String removeFirstBlankLine (String script) {
		
		boolean charFound = false;
		int i = 0;
		
		while (script.charAt(i) != '\n') {
			if (script.charAt(i) == ' '){
				i++;
			} else {
				charFound = true;
				break;
			}
		}
		
		if (!charFound) {
			return script.substring(i+1);
		} else
			return script;
	}
	
	private int countLeadingSpaces (String script) {
		int i = 0;
		while (script.charAt(i) == ' ')
			i++;
		
		return i;
	}
}
