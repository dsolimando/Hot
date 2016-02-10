package be.solidx.hot.shows.groovy;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

import be.solidx.hot.shows.ClosureRequestMapping.Options;
import be.solidx.hot.shows.RestRequest;
import be.solidx.hot.utils.HttpDataDeserializer;
import be.solidx.hot.utils.ScriptMapConverter;

public class GroovyRestRequest extends RestRequest<Map<?, ?>> {
	
	public GroovyRestRequest(
			Options options, 
			ScriptMapConverter<Map<?, ?>> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body,
			Authentication authentication) {
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
		initUser(authentication);
	}
	
	@Override
	public Map<?, ?> getUser() {
		return userAsMap;
	}
}
