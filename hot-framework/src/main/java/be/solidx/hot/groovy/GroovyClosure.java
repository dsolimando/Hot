package be.solidx.hot.groovy;

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

import be.solidx.hot.Closure;

public class GroovyClosure implements Closure {

	groovy.lang.Closure<?> closure;
	
	public GroovyClosure(groovy.lang.Closure<?> closure) {
		this.closure = closure; 
	}

	@Override
	public Object call(Object... objects) {
		return closure.call(objects); 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof GroovyClosure) {
			GroovyClosure groovyClosure = (GroovyClosure) obj;
			return closure.getMetaClass().hashCode() == groovyClosure.closure.getMetaClass().hashCode();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return closure.getMetaClass().hashCode();
	}
}
