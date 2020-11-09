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

import java.net.URL;

import org.springframework.context.ApplicationEvent;

public class ReloadShowEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5914799970174770678L;
	
	URL showUrl;
	
	ReloadReason reloadReason;

	public ReloadShowEvent(Object source, URL showUrl, ReloadReason reason) {
		super(source);
		this.showUrl = showUrl;
		this.reloadReason = reason;
	}
	
	public URL getShowUrl() {
		return showUrl;
	}
	
	public ReloadReason getReloadReason() {
		return reloadReason;
	}
	
	public enum ReloadReason {
		ADDED, MODIFIED, DELETED;
	}
}
