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

import be.solidx.hot.python.PyDictionaryConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.python.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;


public class PythonHttpDataDeserializer extends AbstractHttpDataDeserializer {

	DocumentBuilder documentBuilder;
	
	PyDictionaryConverter pyDataConverter;
	
	public PythonHttpDataDeserializer(ObjectMapper objectMapper, PyDictionaryConverter pyDataConverter) throws ParserConfigurationException {
		super(objectMapper);
		documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
		this.pyDataConverter = pyDataConverter;
	}

	@Override
	synchronized protected PyObject toXML(byte[] data, Charset charset) {
		try {
			return Py.java2py(documentBuilder.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(data),charset))));
		} catch (SAXException | IOException e) {
			throw new DeserializationException(e.getMessage(), e.getCause());
		}
	}
	
	@Override
	protected PyDictionary fromJSON(byte[] data) throws IOException {
		return pyDataConverter.toScriptMap(super.fromJSON(data));
	}

	@Override
	protected Map<?, ?> fromFormUrlEncoded(String data) {
		return pyDataConverter.toScriptMap(super.fromFormUrlEncoded(data));
	}
}
