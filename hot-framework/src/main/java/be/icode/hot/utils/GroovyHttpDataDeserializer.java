package be.icode.hot.utils;

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
