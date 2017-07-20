package be.solidx.hot.shows;

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
import java.util.concurrent.ScheduledFuture;

import be.solidx.hot.data.AsyncDB;
import be.solidx.hot.nio.http.Request;
import be.solidx.hot.nio.http.SSLContextBuilder;
import be.solidx.hot.promises.Promise;


public interface Show<CLOSURE,MAP extends Map<?,?>> {

	Rest<CLOSURE> getRest();
	
	WebSocket<CLOSURE, MAP> getWebsocket();
	
	AsyncDB<CLOSURE,MAP> db(String dbname);
	
	AsyncDB<CLOSURE,MAP> getDb();
	
	ScheduledFuture<?> setInterval(CLOSURE closure, int interval);
	
	ScheduledFuture<?> setTimeout(CLOSURE closure, int delay);
	
	void clearTimeout(ScheduledFuture<?> task);
	
	void clearInterval(ScheduledFuture<?> task);
	
	EventBus<CLOSURE> getEventBus();

    Request<CLOSURE, MAP> http (MAP options) throws Exception;

    Request<CLOSURE, MAP> fetch (String url, MAP options) throws Exception;

	Promise<CLOSURE> blocking (CLOSURE closure);
	
	Promise<CLOSURE> Deferred ();

	void scale ();

	void scale (int numCPU);
}
