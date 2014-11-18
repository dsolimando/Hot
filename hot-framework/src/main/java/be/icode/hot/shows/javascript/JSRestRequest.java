package be.icode.hot.shows.javascript;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
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

public class JSRestRequest extends RestRequest<NativeObject> {

	NativeObject user = new NativeObject();
	
	public JSRestRequest(
			Options options, 
			ScriptMapConverter<NativeObject> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body,
			Authentication authentication) {
		
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
		initUser(authentication);
	}
	
	private void initUser(Authentication authentication) {
		
		if (authentication == null) return ;
		
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			
			if (usernamePasswordAuthenticationToken.getPrincipal() instanceof LdapUserDetailsImpl) {
				LdapUserDetailsImpl detailsImpl = (LdapUserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
				user.put("name", user, detailsImpl.getUsername());
				user.put("password", user, detailsImpl.getPassword());
				user.put("dn",user, detailsImpl.getDn());
				List<String> roles = new ArrayList<>();
				for (GrantedAuthority authority : detailsImpl.getAuthorities()) {
					roles.add(authority.getAuthority());
				}
				user.put("roles", user, new NativeArray(roles.toArray()));
			} else {
				user.put("name", user, usernamePasswordAuthenticationToken.getPrincipal());
				user.put("password", user, usernamePasswordAuthenticationToken.getCredentials());
				List<String> roles = new ArrayList<>();
				for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
					roles.add(authority.getAuthority());
				}
				user.put("roles", user, new NativeArray(roles.toArray()));
			}
		} else if (authentication instanceof SocialAuthenticationToken) {
			SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
			ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
			user.put("name", user, connectionData.getDisplayName());
			user.put("accessToken", user, connectionData.getAccessToken());
			user.put("picture", user, connectionData.getImageUrl());
			user.put("link", user, connectionData.getProfileUrl());
			user.put("id", user, connectionData.getProviderUserId());
			user.put("provider", user, connectionData.getProviderId());
			user.put("expiresIn", user, connectionData.getExpireTime());
		}
	}
	
	@Override
	public NativeObject getUser() {
		return user;
	}
}
