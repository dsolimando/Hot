package be.solidx.hot.data;

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

import java.util.Map;

import be.solidx.hot.promises.Promise;

public interface AsyncCollection<CLOSURE, T extends Map<?,?>> {

	Promise<CLOSURE> findOne (T t);

	Promise<CLOSURE> findOne (T t, CLOSURE sucessCallback);

	Promise<CLOSURE> findOne (T t, CLOSURE sucessCallback, CLOSURE failCallback);
	
	AsyncCursor<CLOSURE,T> find (T t);
	
	AsyncCursor<CLOSURE,T> find ();
	
	Promise<CLOSURE> count (T where);
	
	Promise<CLOSURE> count (T where, CLOSURE successCallback);

	Promise<CLOSURE> count (T where, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> update (T values, T where);
	
	Promise<CLOSURE> remove (T t);
	
	Promise<CLOSURE> remove (T t, CLOSURE successCallback);
	
	Promise<CLOSURE> remove (T t, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> insert (T t);
	
	Promise<CLOSURE> insert (T t, CLOSURE successCallback);
	
	Promise<CLOSURE> insert (T t, CLOSURE successCallback, CLOSURE failCallback);
	
	Promise<CLOSURE> drop ();
	
	Promise<CLOSURE> drop (CLOSURE successCallback);
	
	Promise<CLOSURE> drop (CLOSURE successCallback, CLOSURE failCallback);
}
