package be.icode.hot.shows.python;

import javax.servlet.http.HttpServletRequest;

import org.python.core.PyDictionary;

import be.icode.hot.shows.ClosureRequestMapping.Options;
import be.icode.hot.shows.RestRequest;
import be.icode.hot.utils.HttpDataDeserializer;
import be.icode.hot.utils.ScriptMapConverter;

public class PythonRestRequest extends RestRequest<PyDictionary> {

	public PythonRestRequest(
			Options options, 
			ScriptMapConverter<PyDictionary> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest) {
		
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest);
	}
}
