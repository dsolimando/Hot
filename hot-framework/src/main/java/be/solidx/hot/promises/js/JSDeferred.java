package be.solidx.hot.promises.js;

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

import org.jdeferred.impl.DeferredObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

public class JSDeferred extends JSPromise implements Deferred<NativeFunction> {

	public JSDeferred(Scriptable globalscope) {
		super(new DeferredObject<>(), globalscope);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSDeferred resolve(Object... objects) {
		((DeferredObject)promise).resolve(objects);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSDeferred reject(Object... objects) {
		((DeferredObject)promise).reject(objects);
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JSDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.notify(notificationValues);
		return this;
	}

	@Override
	public Promise<NativeFunction> promise() {
		return this;
	}

}
