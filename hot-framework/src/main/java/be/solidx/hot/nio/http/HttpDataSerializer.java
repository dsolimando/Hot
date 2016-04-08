package be.solidx.hot.nio.http;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.python.core.Py;
import org.python.core.PyInstance;
import org.python.core.PyString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.LinkedMultiValueMap;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import be.solidx.hot.DataConverter;
import be.solidx.hot.utils.XStreamMapEntryConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public class HttpDataSerializer {
	
	public static final Charset defaultCharset = Charset.forName("UTF-8");

	FormHttpMessageConverter formHttpMessageConverter;
	
	ObjectMapper objectMapper;
	
	XStream xStream;
	
	DataConverter dataConverter;
	
	Map<Class<?>, JAXBContext> jaxbContextMap = new HashMap<>();
	
	public static List<String> byteMediaTypes = Arrays.asList(
			MediaType.APPLICATION_OCTET_STREAM.toString(),
			"application/pdf",
			MediaType.IMAGE_JPEG.toString(),
			MediaType.IMAGE_GIF.toString(),
			MediaType.IMAGE_PNG.toString());
	
	@Autowired
	public HttpDataSerializer(FormHttpMessageConverter formHttpMessageConverter, ObjectMapper objectMapper, XStream xStream, DataConverter dataConverter) {
		this.formHttpMessageConverter = formHttpMessageConverter;
		this.objectMapper = objectMapper;
		this.xStream = xStream;
		this.dataConverter = dataConverter;
		
		Object mapConverter = xStream.getConverterLookup().lookupConverterForType(Map.class);
		if (mapConverter != null && !(mapConverter instanceof XStreamMapEntryConverter)) {
			xStream.registerConverter(new XStreamMapEntryConverter());
		}
	}

	public byte[] serialize (Object data, String contentType) throws HttpDataSerializationException {
		MediaType mediaType = MediaType.parseMediaType(contentType);
		return serialize(data, mediaType);
	}
	
	@SuppressWarnings("rawtypes")
	public byte[] serialize (Object data, MediaType mediaType) throws HttpDataSerializationException {
		if (data == null) return "null".getBytes();
		
		try {
			if (data instanceof Map) {
				return serializeMap((Map) data, mediaType);
			}  else {
				return serializeObject(data, mediaType);
			}
		} catch (Exception e) {
			throw new HttpDataSerializationException(e.getMessage(), e.getCause());
		}
	}
	
	private byte[] serializeObject (Object data, MediaType mediaType) throws JAXBException, IOException {
		Charset charset = mediaType.getCharSet() != null?mediaType.getCharSet():defaultCharset;
		
		if (mediaType.getSubtype().equals(MediaType.APPLICATION_XML.getSubtype())) {
			if (data.getClass().getAnnotation(XmlRootElement.class) != null) {
				JAXBContext jaxbContext;
				if (jaxbContextMap.get(data.getClass()) == null) {
					jaxbContext = JAXBContext.newInstance(data.getClass());
					jaxbContextMap.put(data.getClass(), jaxbContext);
				} else {
					jaxbContext = jaxbContextMap.get(data.getClass());
				}
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				jaxbContext.createMarshaller().marshal(data, byteArrayOutputStream);
				byteArrayOutputStream.flush();
				return byteArrayOutputStream.toByteArray();
			} else if (data instanceof Document) {
					return serializeDOM((Document) data, charset);
			} else if (data instanceof List<?>) {
				HashMap<String,Object> wrapper = new HashMap<>();
				wrapper.put("item", data);
				return handleXML(wrapper, mediaType);
			} else if (data instanceof PyInstance && ((PyInstance)data).instclass.__name__.equals("Document")) {
				return ((PyString)((PyInstance)data).invoke("toxml", Py.java2py(charset.toString()))).decode(charset.toString()).toString().getBytes(charset.toString());
			}
			else {
				return data.toString().getBytes(charset);
			}
		} else if (mediaType.getSubtype().equals(MediaType.APPLICATION_JSON.getSubtype())) {
			return objectMapper.writeValueAsBytes(data);
		} else if (byteMediaTypes.contains(mediaType.toString()) && data instanceof byte[]) {
			return (byte[]) data;
		} else {
			return data.toString().getBytes(charset);
		}
	}
	
	private byte[] serializeMap (Map<?,?> data, MediaType mediaType) throws HttpMessageNotWritableException, IOException {
		if (mediaType.getSubtype().equals(MediaType.APPLICATION_FORM_URLENCODED.getSubtype()) || mediaType.getSubtype().equals(MediaType.MULTIPART_FORM_DATA.getSubtype())) {

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			LinkedMultiValueMap<String, ?> multiValueMap = dataConverter.toMultiValueMap(data);
			HttpOutputMessage outputMessage = new HttpOutputMessage(){
				@Override
				public HttpHeaders getHeaders() {
					return new HttpHeaders();
				}
				@Override
				public OutputStream getBody() throws IOException {
					return byteArrayOutputStream;
				}
			};
			formHttpMessageConverter.write(multiValueMap, mediaType, outputMessage);
			return byteArrayOutputStream.toByteArray();
		} else if (mediaType.getSubtype().equals(MediaType.APPLICATION_XML.getSubtype())) {
			return handleXML(data, mediaType);
		} else if (mediaType.getSubtype().equals(MediaType.APPLICATION_JSON.getSubtype())) {
			return objectMapper.writeValueAsBytes(data);
		} else {
			return data.toString().getBytes();
		}
	}
	
	private byte[] handleXML(Map<?,?> data, MediaType mediaType) throws IOException {
		
		Charset charset = mediaType.getCharSet() != null?mediaType.getCharSet():defaultCharset;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new  OutputStreamWriter(byteArrayOutputStream, charset);
		String rootName = "root";
		if (data.keySet().size() == 1) {
			Object value = data.values().iterator().next();
			if (value instanceof Map<?,?>) {
				rootName = ((String) data.keySet().iterator().next());
				xStream.alias(rootName, LinkedHashMap.class);
				xStream.alias(rootName, Map.class);
				xStream.marshal(data.get(rootName), new CompactWriter(outputStreamWriter));
			} else {
				rootName = ((String) data.keySet().iterator().next())+"s";
				xStream.alias(rootName, LinkedHashMap.class);
				xStream.alias(rootName, Map.class);
				xStream.marshal(data, new CompactWriter(outputStreamWriter));
			}
		} else {
			xStream.alias(rootName, LinkedHashMap.class);
			xStream.alias(rootName, Map.class);
			xStream.marshal(data, new CompactWriter(outputStreamWriter));
		}
		
			
//		} else {
//			xStream.alias(firstChildKey.toString(), LinkedHashMap.class);
//			xStream.alias(firstChildKey.toString(), Map.class);
//			xStream.marshal(data, new CompactWriter(outputStreamWriter));
//		}
		outputStreamWriter.flush();
		return byteArrayOutputStream.toByteArray();
	}
	
	private byte[] serializeDOM (Document document, Charset charset) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(document).getBytes(charset);
	}
	
	public static class HttpDataSerializationException extends Exception {

		private static final long serialVersionUID = -1886840943899129240L;

		public HttpDataSerializationException(String message, Throwable cause) {
			super(message, cause);
		}

		public HttpDataSerializationException(Throwable cause) {
			super(cause);
		}
	}
}
