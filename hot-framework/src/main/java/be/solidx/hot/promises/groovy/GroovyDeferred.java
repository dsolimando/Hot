package be.solidx.hot.promises.groovy;

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

import groovy.lang.Closure;

import org.jdeferred.impl.DeferredObject;

import be.solidx.hot.promises.Deferred;
import be.solidx.hot.promises.Promise;

public class GroovyDeferred extends GroovyPromise implements Deferred<Closure<?>> {

	public GroovyDeferred() {
		super(new DeferredObject<>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public GroovyDeferred resolve(Object... resolveValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (resolveValues.length == 1) {
			deferredObject.resolve(resolveValues[0]);
		} else {
			deferredObject.resolve(resolveValues);
		}
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public GroovyDeferred reject(Object... rejectValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (rejectValues.length == 1) {
			deferredObject.reject(rejectValues[0]);
		} else {
			deferredObject.reject(rejectValues);
		}
		return this;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public GroovyDeferred notify(Object... notificationValues) {
		DeferredObject deferredObject = (DeferredObject) super.promise;
		if (notificationValues.length == 1) {
			deferredObject.notify(notificationValues[0]);
		} else {
			deferredObject.notify(notificationValues);
		}
		return this;
	}

	@Override
	public Promise<Closure<?>> promise() {
		return this;
	}
}
