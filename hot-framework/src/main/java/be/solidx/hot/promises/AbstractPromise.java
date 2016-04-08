package be.solidx.hot.promises;

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

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;


public abstract class AbstractPromise<CLOSURE> implements Promise<CLOSURE> {

	@SuppressWarnings("rawtypes")
	protected org.jdeferred.Promise promise;
	
	@SuppressWarnings("rawtypes")
	public AbstractPromise(org.jdeferred.Promise promise) {
		this.promise = promise;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Promise<CLOSURE> _done(final DCallback callback) {
		promise.done(new DoneCallback<Object>() {
			@Override
			public void onDone(Object result) {
				callback.onDone(result);
			}
		});
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Promise<CLOSURE> _fail(final FCallback callback) {
		promise.fail(new FailCallback<Object>() {
			@Override
			public void onFail(Object throwable) {
				callback.onFail(throwable);
			}
		});
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	public org.jdeferred.Promise getPromise() {
		return promise;
	}
}
