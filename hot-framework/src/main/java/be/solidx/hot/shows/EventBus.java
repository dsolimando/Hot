package be.solidx.hot.shows;

public interface EventBus<CLOSURE> {

	void on (String event, CLOSURE closure);
	
	void off (String event);
	
	void trigger (String event, Object data);
}
