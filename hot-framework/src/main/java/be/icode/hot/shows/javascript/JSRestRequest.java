package be.icode.hot.shows.javascript;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.NativeObject;
import org.springframework.web.context.request.WebRequest;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.shows.RestRequest;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

public class JSRestRequest extends RestRequest<NativeObject> {

	public JSRestRequest(
			Options options, 
			ScriptMapConverter<NativeObject> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest) {
		
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest);
	}
}
