package be.solidx.hot.data.mongo;

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

import java.util.Map;

import be.solidx.hot.data.AsyncCollection;
import be.solidx.hot.promises.Promise;

public interface MongoAsyncCollection<CLOSURE,T extends Map<?, ?>> extends AsyncCollection<CLOSURE, T> {

    Promise<CLOSURE> count ();
    Promise<CLOSURE> count (CLOSURE successCallback);
    Promise<CLOSURE> count (CLOSURE successCallback, CLOSURE errorCallback);

	Promise<CLOSURE> save (T t);
	Promise<CLOSURE> save (T t,CLOSURE successCallback);
	Promise<CLOSURE> save (T t,CLOSURE successCallback, CLOSURE errorCallback);
	
	Promise<CLOSURE> update (T q, T d, boolean upsert, boolean multi);
	
	Promise<CLOSURE> runCommand (String command, T t);
	Promise<CLOSURE> runCommand (String command, T t, CLOSURE successCallback);
	Promise<CLOSURE> runCommand (String command, T t, CLOSURE successCallback, CLOSURE errorCallback);
}
