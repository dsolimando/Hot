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

import java.io.IOException;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class RhinoJsLoader {

	private String rootPath;
	
	private Context context;

	private Scriptable scope;

	public RhinoJsLoader(String rootPath, Context context, Scriptable scope) {
		this.rootPath = rootPath;
		this.context = context;
		this.scope = scope;
	}
	
	public RhinoJsLoader(Context context, Scriptable scope) {
		this.rootPath = "";
		this.context = context;
		this.scope = scope;
	}
	
	public void load (String path) throws IOException {
		context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream(rootPath+"/"+path)), path, 1, null);
	}
}
