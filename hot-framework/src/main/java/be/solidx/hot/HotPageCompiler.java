package be.solidx.hot;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class HotPageCompiler {

	private static final Logger logger = LoggerFactory.getLogger(HotPageCompiler.class);

	private boolean devMode = false;

	protected Map<String, Script<String>> compilableMap = new ConcurrentHashMap<String, Script<String>>();
	
	private Map<String,Boolean> hasSubscriptMap = new ConcurrentHashMap<String, Boolean>();

	public String compile(Script<String> script) {
		Script<String> compiledScript = compilableMap.get(script.getName());
		if ((compiledScript != null && !devMode) || 
				(compiledScript != null && devMode && !script.outdated(compiledScript.getCodeUTF8())) && !hasSubscriptMap.get(script.getName())) {
			return compiledScript.getCompiledScript();
		} else {
			synchronized (compilableMap) {
				if ((compiledScript != null && !devMode) || 
				(compiledScript != null && devMode && !script.outdated(compiledScript.getCodeUTF8())) && !hasSubscriptMap.get(script.getName())){
					return compiledScript.getCompiledScript();
				} else {
					script.setCompiledScript(compilePage(script));
					compilableMap.put(script.getName(), script);
					return script.getCompiledScript();
				}
			}
		}
	}

	private String compilePage(Script<String> script) {

		String hotPage = script.getCodeUTF8();
		// Script has not changed
		StringBuffer stringBuffer = new StringBuffer();

		// Search embedded scripts
		int offset = 0;
		int hotStartIndex;
		hasSubscriptMap.put(script.getName(), false);
		while (true) {
			hotStartIndex = hotPage.indexOf("<?hot", offset);

			if (hotStartIndex < 0)
				break;

			int hotEndIndex = hotPage.indexOf("?>", hotStartIndex);
			String html = hotPage.substring(offset, hotStartIndex);

			if (!html.equals(""))
				stringBuffer.append(print(html));
			if (hotPage.substring(hotStartIndex, hotStartIndex + 13).equals("<?hot-include")) {
				hasSubscriptMap.put(script.getName(), true);
				String scriptName = hotPage.substring(hotStartIndex + 13, hotEndIndex);
				Script<String> subScript;
				try {
					subScript = new Script<String>(IOUtils.toByteArray(be.solidx.hot.utils.IOUtils.loadResourceNoCache(scriptName.trim())), scriptName);
					stringBuffer.append(compile(subScript)).append("\n");
				} catch (IOException e) {
					logger.error("Failed to load subscript "+scriptName);
				}
			} else
				stringBuffer.append(printScript(hotPage.substring(hotStartIndex + 5, hotEndIndex)));

			offset = hotEndIndex + 2;
			hotStartIndex = hotEndIndex;
		}
		String tail = hotPage.substring(offset);

		if (!tail.equals(""))
			stringBuffer.append(print(tail));

		return stringBuffer.toString();
	}

	protected String printScript(String toPrint) {
		return toPrint.trim() + "\n";
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

	protected abstract String print(String toPrint);
}
