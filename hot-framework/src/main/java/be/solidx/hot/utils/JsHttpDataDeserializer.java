package be.solidx.hot.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.NativeObject;

import be.solidx.hot.Script;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.js.JsMapConverter;

public class JsHttpDataDeserializer extends AbstractHttpDataDeserializer {

	JSScriptExecutor jsScriptExecutor;
	
	JsMapConverter jsDataConverter;
	
	public JsHttpDataDeserializer(ObjectMapper objectMapper, JsMapConverter jsDataConverter, JSScriptExecutor jsScriptExecutor) {
		super(objectMapper);
		this.jsScriptExecutor = jsScriptExecutor;
		this.jsDataConverter = jsDataConverter;
	}
	
	@Override
	protected Object toXML(byte[] data, Charset charset) {
		String xml = new String(data, charset);
		// We strip <?XML header
		if (xml.indexOf("<?xml") > -1 || xml.indexOf("<?XML") > -1)
			xml = xml.split("\\?>")[1];
		return jsScriptExecutor.execute(new Script<org.mozilla.javascript.Script>(("var a = " + xml + ";\n a;").getBytes(charset), "xmlParsing"));
	}
	
	@Override
	protected Map<?, ?> fromFormUrlEncoded(String data) {
		return jsDataConverter.toScriptMap(super.fromFormUrlEncoded(data));
	}
	
	@Override
	protected NativeObject fromJSON(byte[] data) throws IOException {
		return jsDataConverter.toScriptMap(super.fromJSON(data));
	}
}
