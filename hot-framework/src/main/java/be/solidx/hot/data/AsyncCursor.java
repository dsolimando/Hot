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

public interface AsyncCursor<CLOSURE, T extends Map<?, ?>> {
	
	Promise<CLOSURE> promise();
	
	Promise<CLOSURE> promise(CLOSURE successClosure);
	
	Promise<CLOSURE> promise(CLOSURE successClosure, CLOSURE failClosure);
	
	Promise<CLOSURE> count(CLOSURE successClosure, CLOSURE failClosure);
	
	Promise<CLOSURE> count(CLOSURE successClosure);
	
	Promise<CLOSURE> count();
	
	AsyncCursor<CLOSURE,T> limit(Integer limit);
	
	AsyncCursor<CLOSURE,T> skip(Integer at);
	
	AsyncCursor<CLOSURE,T> sort(T sortMap);
}
