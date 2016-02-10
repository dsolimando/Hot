package be.solidx.hot.shows.python;

import javax.servlet.http.HttpServletRequest;

import org.python.core.PyDictionary;
import org.springframework.security.core.Authentication;

import be.solidx.hot.shows.ClosureRequestMapping.Options;
import be.solidx.hot.shows.RestRequest;
import be.solidx.hot.utils.HttpDataDeserializer;
import be.solidx.hot.utils.ScriptMapConverter;

public class PythonRestRequest extends RestRequest<PyDictionary> {

	PyDictionary user = new PyDictionary();
	
	public PythonRestRequest(
			Options options, 
			ScriptMapConverter<PyDictionary> scriptMapConverter, 
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
		user = scriptMapConverter.toScriptMap(userAsMap);
	}
	
	@Override
	public PyDictionary getUser() {
		return user;
	}
}
