package be.solidx.hot.promises.python;

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

import org.jdeferred.impl.DeferredObject;
import org.python.core.PyFunction;

import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

public class PythonDeferred extends PythonPromise implements Deferred<PyFunction> {

	public PythonDeferred() {
		super(new DeferredObject<>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public PythonDeferred resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.resolve(resolveValues);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public PythonDeferred reject(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(resolveValues);
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public PythonDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		deferredObject.reject(notificationValues);
		return this;
	}

	@Override
	public Promise<PyFunction> promise() {
		return this;
	}

}
