package be.solidx.hot.python;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import be.solidx.hot.HotPageCompiler;

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
