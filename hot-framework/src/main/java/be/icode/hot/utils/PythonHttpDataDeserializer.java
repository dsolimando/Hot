package be.icode.hot.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.map.ObjectMapper;
import org.python.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import be.icode.hot.python.PyDictionaryConverter;


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
	protected PyDictionary toJSON(byte[] data) throws IOException {
		return pyDataConverter.toScriptMap(super.toJSON(data));
	}

}
