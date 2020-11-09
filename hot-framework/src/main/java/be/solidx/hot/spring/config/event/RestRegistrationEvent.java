package be.solidx.hot.spring.config.event;

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
