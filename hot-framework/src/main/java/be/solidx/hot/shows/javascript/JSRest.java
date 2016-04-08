package be.solidx.hot.shows.javascript;

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

import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.Closure;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.shows.AbstractRest;

public class JSRest extends AbstractRest<NativeFunction> {
	
	Scriptable globalScope;
	
	public JSRest(ExecutorService eventLoop, Scriptable globalScope) {
		super(eventLoop);
		this.globalScope = globalScope;
	}

	@Override
	protected Closure buildShowClosure(NativeFunction closure) {
		return new JSClosure(closure,globalScope);
	}
}
