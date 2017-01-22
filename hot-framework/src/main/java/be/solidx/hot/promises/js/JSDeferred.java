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
import org.mozilla.javascript.Context;
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
        Context.enter();
	    try {
            if (objects.length == 0)
                ((DeferredObject)promise).resolve(null);
            else if (objects.length == 1)
                ((DeferredObject)promise).resolve(Context.javaToJS(objects[0],globalScope));
            else
                ((DeferredObject)promise).resolve(objects);
        } finally {
            Context.exit();
        }
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSDeferred reject(Object... objects) {
        Context.enter();
        try {
            if (objects.length == 0)
                ((DeferredObject)promise).reject(null);
            else if (objects.length == 1)
                ((DeferredObject)promise).reject(Context.javaToJS(objects[0],globalScope));
            else
                ((DeferredObject)promise).reject(objects);
        } finally {
            Context.exit();
        }
        return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JSDeferred notify(Object... notificationValues) {
        Context.enter();
        try {
            DeferredObject deferredObject = (DeferredObject) super.promise;
            if (notificationValues.length == 0)
                ((DeferredObject)promise).notify(null);
            else if (notificationValues.length == 1)
                ((DeferredObject)promise).notify(Context.javaToJS(notificationValues[0],globalScope));
            else
                ((DeferredObject)promise).notify(notificationValues);
        } finally {
            Context.exit();
        }
        return this;
	}

	@Override
	public Promise<NativeFunction> promise() {
		return this;
	}

}
