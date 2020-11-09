package be.solidx.hot.js;

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

import be.solidx.hot.promises.Promise;
import be.solidx.hot.promises.js.JSDeferred;
import be.solidx.hot.promises.js.JSPromise;
import org.mozilla.javascript.*;

import be.solidx.hot.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSClosure implements Closure {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSClosure.class);

	protected NativeFunction nativeFunction;
	protected Scriptable globalscope;
	protected boolean async;
	
	public JSClosure(NativeFunction nativeFunction, Scriptable globalscope) {
		this.nativeFunction = nativeFunction;
		this.globalscope = globalscope;
	}

	public JSClosure(NativeFunction nativeFunction, Scriptable globalscope, boolean async) {
		this.nativeFunction = nativeFunction;
		this.globalscope = globalscope;
		this.async = async;
	}

	@Override
	public Object call(Object...objects) {
        final Context context = Context.enter();
        try {
            if (async) {
                JSDeferred jd = new JSDeferred(globalscope);
                resumeContinuation(jd, null, null, objects);
                return jd;
            }
            else {
                return nativeFunction.call(context, globalscope, nativeFunction, objects);
            }
        } finally {
            Context.exit();
        }
    }

	private void resumeContinuation (final JSDeferred resultDeferred, ContinuationPending pending, NativeArray a ,Object...objects) {
        final Context context = Context.enter();
	    try {
            Object result;
            if (pending == null) {
                // initial function call
                result = context.callFunctionWithContinuations(nativeFunction,globalscope,objects);
            } else {
                result = context.resumeContinuation(pending.getContinuation(),globalscope,a);
            }
            resultDeferred.resolve(result);
        } catch (final ContinuationPending pending1) {
            JSPromise d1 = (JSPromise) pending1.getApplicationState();
            d1._done(new Promise.DCallback() {
                @Override
                public void onDone(Object result) {
                    Object[] args = new Object[2];
                    args[0] = result;
                    args[1] = null;
                    NativeArray a = new NativeArray(args);
                    resumeContinuation(resultDeferred, pending1, a);
                }
            })._fail(new Promise.FCallback() {
                @Override
                public void onFail(Object throwable) {
                    Object[] args = new Object[2];
                    args[1] = throwable;
                    args[0] = null;
                    NativeArray a = new NativeArray(args);
                    resumeContinuation(resultDeferred, pending1, a);
                }
            });
        } finally {
	        Context.exit();
        }
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
