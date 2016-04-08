package be.solidx.hot.js;

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

import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JSLoader {
	
	private ScriptEngine scriptEngine;

	private String rootPath;

	public JSLoader(ScriptEngine scriptEngine, String rootPath) {
		this.scriptEngine = scriptEngine;
		this.rootPath = rootPath;
	}
	
	public JSLoader(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
		this.rootPath = "";
	}

	public void load(String path) throws ScriptException {
		scriptEngine.eval(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(rootPath + "/" + path)));
	}
}
