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

public interface Collection<T extends Map<?,?>> {

	T findOne (T t);
	
	Cursor<T> find (T t);
	
	Cursor<T> find ();
	
	long count (T where);
	
	T update ( T where, T values);
	
	Collection<T> remove (T t);
	
	T insert (T t);
	
	Collection<T> drop ();
}
