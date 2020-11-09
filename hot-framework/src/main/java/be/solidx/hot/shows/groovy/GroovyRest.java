package be.solidx.hot.shows.groovy;

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

import java.util.concurrent.ExecutorService;

import be.solidx.hot.Closure;
import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.shows.AbstractRest;

public class GroovyRest extends AbstractRest<groovy.lang.Closure<?>> {

	public GroovyRest(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Closure buildShowClosure(groovy.lang.Closure<?> closure, boolean async) {
		return new GroovyClosure(closure);
	}
}
