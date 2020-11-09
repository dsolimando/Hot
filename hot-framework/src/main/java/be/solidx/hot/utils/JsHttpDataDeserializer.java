package be.solidx.hot.utils;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import be.solidx.hot.Script;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.js.JsMapConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.NativeObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

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
