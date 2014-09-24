package be.icode.hot.test.util;

import static org.junit.Assert.*

import javax.script.CompiledScript

import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter

import be.icode.hot.DataConverter
import be.icode.hot.js.JSScriptExecutor
import be.icode.hot.js.JsMapConverter
import be.icode.hot.nio.http.HttpDataSerializer
import be.icode.hot.python.PyDictionaryConverter
import be.icode.hot.python.PythonScriptExecutor

import com.thoughtworks.xstream.XStream


class TestHttpDataSerializer {

	FormHttpMessageConverter converter = new FormHttpMessageConverter()
	ObjectMapper objectMapper = new ObjectMapper()
	XStream xStream = new XStream()
	DataConverter dataConverter = new DataConverter()
	HttpDataSerializer dataSerializer = new HttpDataSerializer(converter, objectMapper, xStream, dataConverter)
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor()
	JsMapConverter jsDataConverter = new JsMapConverter()
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor()
	PyDictionaryConverter pyDataConverter = new PyDictionaryConverter()
	
	@Test
	void testGroovySerializationUrlEncode() {
		def data = [name:"damien",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]
		def url = new String(dataSerializer.serialize(data, "application/x-www-form-urlencoded"))
		assert 'name=damien&age=8&values=toto&values=titi&objects%5Bsub1%5D=toto&objects%5Bsub2%5D=titi' == url
	}
	
	@Test
	void testGroovySerializationJSON() {
		def data = [name:"damien",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]
		def url = new String(dataSerializer.serialize(data, "application/json"))
		assert '{"name":"damien","age":8,"values":["toto","titi"],"objects":{"sub1":"toto","sub2":"titi"}}' == url
	}
	
	@Test
	void testGroovySerializationXML() {
		def data = [name:"damién",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<root><name>damién</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>' == url
	}
	
	@Test
	void testGroovySerializationXMLRoot() {
		def data = [values:[name:"damien",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<values><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></values>' == url
	}
	
	@Test
	void testJSSerializationUrlEncode() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.icode.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(data, "application/x-www-form-urlencoded"))
		assert 'name=damien&age=8&values=toto&values=titi&objects%5Bsub1%5D=toto&objects%5Bsub2%5D=titi' == url
	}

	@Test
	void testJSSerializationJSON() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.icode.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(data, "application/json"))
		assert '{"name":"damien","age":8,"values":["toto","titi"],"objects":{"sub1":"toto","sub2":"titi"}}' == url
	}
	
	@Test
	void testJSSerializationXML() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.icode.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toMap(data), "application/xml"))
		print url
		assert '<root><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>' == url
	}
	
	@Test
	void testJSSerializationXMLRoot() {
		String js = "var o = { values: { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}}};\n o";
		def data = jsScriptExecutor.execute(new be.icode.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toMap(data), "application/xml"))
		print url
		assert '<values><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></values>' == url
	}
	
	@Test
	void testPythonSerializationUrlEncode() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.icode.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(res.o, "application/x-www-form-urlencoded"))
		assert 'values=toto&values=titi&objects%5Bsub2%5D=titi&objects%5Bsub1%5D=toto&age=8&name=damien' == url
	}
	
	@Test
	void testPythonSerializationJson() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.icode.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(res.o, "application/json"))
		print url
		assert '{"values":["toto","titi"],"objects":{"sub2":"titi","sub1":"toto"},"age":8,"name":"damien"}' == url
	}
	
	@Test
	void testPythonSerializationXML() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.icode.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(pyDataConverter.toMap(res.o), "application/xml"))
		print url
		assert '<root><values>toto</values><values>titi</values><objects><sub2>titi</sub2><sub1>toto</sub1></objects><age>8</age><name>damien</name></root>' == url
	}
	
	@Test
	void testPythonSerializationXMLRoot() {
		def input = ['o':null]
		String python = "o = { 'values':{'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}}";
		def res = pythonScriptExecutor.execute(new be.icode.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(pyDataConverter.toMap(res.o), "application/xml"))
		print url
		assert '<values><values>toto</values><values>titi</values><objects><sub2>titi</sub2><sub1>toto</sub1></objects><age>8</age><name>damien</name></values>' == url
	}
}
