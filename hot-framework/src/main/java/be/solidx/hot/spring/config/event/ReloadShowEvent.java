package be.solidx.hot.spring.config.event;

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
