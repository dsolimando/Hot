package be.icode.hot.shows.groovy;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.shows.RestRequest;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

public class GroovyRestRequest extends RestRequest<Map<?, ?>> {

	public GroovyRestRequest(
			Options options, 
			ScriptMapConverter<Map<?, ?>> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest) {
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest);
	}
}
