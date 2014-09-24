package be.icode.hot.shows;

import java.util.Map;

import be.icode.hot.shows.AbstractWebSocket.Options;

public interface WebSocket<CLOSURE,MAP> {
	
	public static final String OPTION_PATH = "path";

	Handler<CLOSURE> addHandler (MAP options);
	
	public interface Handler<CLOSURE> {
		Connection<CLOSURE> connect(CLOSURE onConnect);
	}
	
	public interface Connection<CLOSURE> {
		void close(CLOSURE onClose);
		void data(CLOSURE onMessage);
		void write (String message) throws Exception;
	}

	Map<Options, Handler<CLOSURE>> getSocketHandlerAdapterMap();
}
