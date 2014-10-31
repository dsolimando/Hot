package be.icode.hot.shows.python;

import javax.servlet.http.HttpServletRequest;

import org.python.core.PyDictionary;
import org.python.core.PyList;
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

public class PythonRestRequest extends RestRequest<PyDictionary> {

	public PythonRestRequest(
			Options options, 
			ScriptMapConverter<PyDictionary> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body) {
		
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
	}
	
	@Override
	public PyDictionary getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null) return null;
		
		PyDictionary map = new PyDictionary();
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			map.put("name", usernamePasswordAuthenticationToken.getPrincipal());
			map.put("password", usernamePasswordAuthenticationToken.getCredentials());
			PyList roles = new PyList();
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
