package be.solidx.hot.shows.javascript;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.NativeObject;
import org.springframework.security.core.Authentication;

import be.solidx.hot.shows.ClosureRequestMapping.Options;
import be.solidx.hot.shows.RestRequest;
import be.solidx.hot.utils.HttpDataDeserializer;
import be.solidx.hot.utils.ScriptMapConverter;

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
	
	@Override
	protected void initUser(Authentication authentication) {
		// TODO Auto-generated method stub
		super.initUser(authentication);
		user = scriptMapConverter.toScriptMap(super.userAsMap);
	}
	
	@Override
	public NativeObject getUser() {
		return user;
	}
}
