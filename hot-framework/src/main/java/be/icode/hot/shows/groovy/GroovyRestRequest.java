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
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.shows.RestRequest;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

public class GroovyRestRequest extends RestRequest<Map<?, ?>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyRestRequest.class);
	
	Map<String, Object> user = new HashMap<>();

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
	
	private void initUser(Authentication authentication) {
		
		if (authentication == null) return;
		
		LOGGER.debug("Authentication Type: "+authentication.getClass());
		
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			
			if (usernamePasswordAuthenticationToken.getPrincipal() instanceof LdapUserDetailsImpl) {
				LdapUserDetailsImpl detailsImpl = (LdapUserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
				user.put("name", detailsImpl.getUsername());
				user.put("password", detailsImpl.getPassword());
				user.put("dn", detailsImpl.getDn());
				List<String> roles = new ArrayList<>();
				for (GrantedAuthority authority : detailsImpl.getAuthorities()) {
					roles.add(authority.getAuthority());
				}
				user.put("roles", roles);
			} else {
				user.put("name", usernamePasswordAuthenticationToken.getPrincipal());
				user.put("password", usernamePasswordAuthenticationToken.getCredentials());
				List<String> roles = new ArrayList<>();
				for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
					roles.add(authority.getAuthority());
				}
				user.put("roles", roles);
			}
			
		} else if (authentication instanceof SocialAuthenticationToken) {
			SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
			ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
			user.put("name", connectionData.getDisplayName());
			user.put("accessToken", connectionData.getAccessToken());
			user.put("picture", connectionData.getImageUrl());
			user.put("link", connectionData.getProfileUrl());
			user.put("id", connectionData.getProviderUserId());
			user.put("provider", connectionData.getProviderId());
			user.put("expiresIn", connectionData.getExpireTime());
		}
	}
	
	@Override
	public Map<?, ?> getUser() {
		return user;
	}
}
