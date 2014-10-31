package be.icode.hot.shows.groovy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.shows.RestRequest;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

public class GroovyRestRequest extends RestRequest<Map<?, ?>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyRestRequest.class);

	public GroovyRestRequest(
			Options options, 
			ScriptMapConverter<Map<?, ?>> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body) {
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
	}
	
	@Override
	public Map<String, Object> getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		LOGGER.debug("Authentication Object found in context holder: "+authentication);
		
		if (authentication == null) return null;
		
		LOGGER.debug("Authentication Type: "+authentication.getClass());

		
		Map<String, Object> map = new HashMap<>();
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			map.put("name", usernamePasswordAuthenticationToken.getPrincipal());
			map.put("password", usernamePasswordAuthenticationToken.getCredentials());
			List<String> roles = new ArrayList<>();
			for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
			map.put("roles", roles);
		} else if (authentication instanceof SocialAuthenticationToken) {
			SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
			ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
			map.put("name", connectionData.getDisplayName());
			map.put("accessToken", connectionData.getAccessToken());
			map.put("picture", connectionData.getImageUrl());
			map.put("link", connectionData.getProfileUrl());
			map.put("id", connectionData.getProviderUserId());
			map.put("provider", connectionData.getProviderId());
			map.put("expiresIn", connectionData.getExpireTime());
		}
		return map;
	}
}
