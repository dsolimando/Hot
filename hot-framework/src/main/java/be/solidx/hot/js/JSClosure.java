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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.Closure;

public class JSClosure implements Closure {

	protected NativeFunction nativeFunction;
	protected Scriptable globalscope;
	
	public JSClosure(NativeFunction nativeFunction, Scriptable globalscope) {
		this.nativeFunction = nativeFunction;
		this.globalscope = globalscope;
	}

	@Override
	public Object call(Object...objects) {
		Context context = Context.enter();
		Object object = nativeFunction.call(context,globalscope,nativeFunction, objects);
		Context.exit();
		return object;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof JSClosure) {
			JSClosure jsClosure = (JSClosure) obj;
			return jsClosure.nativeFunction.getEncodedSource().hashCode() == nativeFunction.getEncodedSource().hashCode();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return nativeFunction.getEncodedSource().hashCode();
	}
}
