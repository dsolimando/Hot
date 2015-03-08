package be.solidx.hot.spring.config.event;

import org.springframework.context.ApplicationEvent;

import be.solidx.hot.shows.Show;

public class RestRegistrationEvent extends ApplicationEvent {

	Show<?, ?> show;
	
	Action action;
	
	private static final long serialVersionUID = -8913358856166967319L;

	public RestRegistrationEvent(Object source, Show<?, ?> show, Action action) {
		super(source);
		this.show = show;
		this.action = action;
	}

	public Show<?, ?> getShow() {
		return show;
	}
	
	public Action getAction() {
		return action;
	}
	
	public enum Action {
		CREATE, UPDATE, REMOVE
	}
}
