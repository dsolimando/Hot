package be.solidx.hot.utils;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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

import groovy.util.XmlSlurper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.SAXException;

public class GroovyHttpDataDeserializer extends AbstractHttpDataDeserializer {
	
	XmlSlurper xmlSlurper;

	public GroovyHttpDataDeserializer(ObjectMapper objectMapper) throws ParserConfigurationException, SAXException {
		super(objectMapper);
		xmlSlurper = new XmlSlurper();
	}

	@Override
	synchronized protected Object toXML(byte[] data, Charset charset) {
		try {
			return xmlSlurper.parse(new InputStreamReader(new ByteArrayInputStream(data), charset));
		} catch (IOException | SAXException e) {
			throw new DeserializationException(e.getMessage(), e.getCause());
		}
	}

}
