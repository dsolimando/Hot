package be.icode.hot.shows;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import be.icode.hot.data.AsyncDB;
import be.icode.hot.nio.http.Request;
import be.icode.hot.promises.Promise;


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
	
	Request<CLOSURE, MAP> http (MAP options);
	
	Promise<CLOSURE> blocking (CLOSURE closure);
}