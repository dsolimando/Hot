package be.icode.hot.shows.javascript;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
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

public class JSRestRequest extends RestRequest<NativeObject> {

	public JSRestRequest(
			Options options, 
			ScriptMapConverter<NativeObject> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body) {
		
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
	}
	
	@Override
	public NativeObject getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null) return null;
		
		NativeObject map = new NativeObject();
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			map.put("name", map, usernamePasswordAuthenticationToken.getPrincipal());
			map.put("password", map, usernamePasswordAuthenticationToken.getCredentials());
			List<String> roles = new ArrayList<>();
			for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
			map.put("roles", map, new NativeArray(roles.toArray()));
		} else if (authentication instanceof SocialAuthenticationToken) {
			SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
			ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
			map.put("name", map, connectionData.getDisplayName());
			map.put("accessToken", map, connectionData.getAccessToken());
			map.put("picture", map, connectionData.getImageUrl());
			map.put("link", map, connectionData.getProfileUrl());
			map.put("id", map, connectionData.getProviderUserId());
			map.put("provider", map, connectionData.getProviderId());
			map.put("expiresIn", map, connectionData.getExpireTime());
		}
		return map;
	}
}
