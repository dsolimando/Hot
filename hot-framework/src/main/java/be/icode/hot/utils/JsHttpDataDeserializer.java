package be.icode.hot.utils;

import java.io.IOException;
import java.nio.charset.Charset;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.NativeObject;

import be.icode.hot.Script;
import be.icode.hot.js.JSScriptExecutor;
import be.icode.hot.js.JsMapConverter;

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
		return jsScriptExecutor.execute(new Script<org.mozilla.javascript.Script>(("var a = "+new String(data, charset)+";\n a;").getBytes(charset), "xmlParsing"));
	}
	
	@Override
	protected NativeObject toJSON(byte[] data) throws IOException {
		return jsDataConverter.toScriptMap(super.toJSON(data));
	}
}
