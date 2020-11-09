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

package be.solidx.hot.test.util

import be.solidx.hot.DataConverter
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.js.JsMapConverter
import be.solidx.hot.nio.http.HttpDataSerializer
import be.solidx.hot.python.PyDictionaryConverter
import be.solidx.hot.python.PythonScriptExecutor
import com.fasterxml.jackson.databind.ObjectMapper
import com.thoughtworks.xstream.XStream
import org.junit.Test
import org.springframework.http.converter.FormHttpMessageConverter

import javax.script.CompiledScript;

class TestHttpDataSerializer {

	FormHttpMessageConverter converter = new FormHttpMessageConverter()
    ObjectMapper objectMapper = new ObjectMapper()
	XStream xStream = new XStream()
	DataConverter dataConverter = new DataConverter()
    PyDictionaryConverter pyDictionaryConverter = new PyDictionaryConverter()
    JsMapConverter jsMapConverter = new JsMapConverter()
	HttpDataSerializer dataSerializer = new HttpDataSerializer(converter, objectMapper, xStream, dataConverter, jsMapConverter, pyDictionaryConverter)
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
		assert '{"name":"damien","age":8,"values":["toto","titi"],"objects":{"sub2":"titi","sub1":"toto"}}' == url
	}
	
	@Test
	void testGroovySerializationXML() {
		def data = [name:"damien",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<root><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>' == url
	}
	
	@Test
	void testGroovySerializationXMLArray() {
		def data = [
			[name:"damien",age:8, totos:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]],
			[name:"toto",age:8, totos:["titi","titi"], objects:[sub1:"toto",sub2:"titi"]]
		]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<items>'+
				'<item><name>damien</name><age>8</age><totos>toto</totos><totos>titi</totos><objects><sub1>toto</sub1><sub2>titi</sub2></objects></item>'+
				'<item><name>toto</name><age>8</age><totos>titi</totos><totos>titi</totos><objects><sub1>toto</sub1><sub2>titi</sub2></objects></item>'+
				'</items>' == url
	}
	
	@Test
	void testGroovySerializationXMLArrayWrapper() {
		def data = [ personne:[
			[name:"damien",age:8, nom:["toto","titi"], object:[sub1:"toto",sub2:"titi"]],
			[name:"toto",age:8, nom:["titi","titi"], object:[sub1:"toto",sub2:"titi"]]
			]
		]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<personnes>'+
				'<personne><name>damien</name><age>8</age><nom>toto</nom><nom>titi</nom><object><sub1>toto</sub1><sub2>titi</sub2></object></personne>'+
				'<personne><name>toto</name><age>8</age><nom>titi</nom><nom>titi</nom><object><sub1>toto</sub1><sub2>titi</sub2></object></personne>'+
				'</personnes>' == url
	}
	
	@Test
	void testGroovySerializationXMLRoot() {
		def data = [value:[name:"damien",age:8, values:["toto","titi"], objects:[sub1:"toto",sub2:"titi"]]]
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		assert '<value><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></value>' == url
	}
	
	@Test
	void testJSSerializationUrlEncode() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(data, "application/x-www-form-urlencoded"))
		assert 'name=damien&age=8&values=toto&values=titi&objects%5Bsub1%5D=toto&objects%5Bsub2%5D=titi' == url
	}

	@Test
	void testJSSerializationJSON() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(data, "application/json"))
		assert '{"name":"damien","age":8,"values":["toto","titi"],"objects":{"sub2":"titi","sub1":"toto"}}' == url
	}
	
	@Test
	void testJSSerializationXML() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toMap(data), "application/xml"))
		print url
		assert '<root><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>' == url
	}
	
	@Test
	void testJSSerializationXMLRoot() {
		String js = "var o = { name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}};\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toMap(data), "application/xml"))
		print url
		assert '<root><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>' == url
	}
	
	@Test
	void testJSSerializationE4X() {
		String js = "var o = <root><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></root>;\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(data, "application/xml"))
		print url
		assert '''<root>
  <name>damien</name>
  <age>8</age>
  <values>toto</values>
  <values>titi</values>
  <objects>
    <sub1>toto</sub1>
    <sub2>titi</sub2>
  </objects>
</root>''' == url
	}
	
	@Test
	void testJSSerializationXMLArray() {
		String js = "var o = [{ name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}},{ name:'julie',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}}];\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toListMap(data), "application/xml"))
		print url
		assert '<items><item><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></item><item><name>julie</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></item></items>' == url
	}
	
	@Test
	void testJSSerializationXMLArrayWrapper() {
		String js = "var o = {personne:[{ name:'damien',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}},{ name:'julie',age:8, values:['toto','titi'], objects:{sub1:'toto', sub2:'titi'}}]};\n o";
		def data = jsScriptExecutor.execute(new be.solidx.hot.Script<org.mozilla.javascript.Script>(js.bytes,"script"))
		def url = new String(dataSerializer.serialize(jsDataConverter.toMap(data), "application/xml"))
		print url
		assert '<personnes><personne><name>damien</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></personne><personne><name>julie</name><age>8</age><values>toto</values><values>titi</values><objects><sub1>toto</sub1><sub2>titi</sub2></objects></personne></personnes>' == url
	}
	
	@Test
	void testPythonSerializationUrlEncode() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(res.o, "application/x-www-form-urlencoded"))
		assert 'values=toto&values=titi&objects%5Bsub2%5D=titi&objects%5Bsub1%5D=toto&name=damien&age=8' == url
	}
	
	@Test
	void testPythonSerializationJson() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(res.o, "application/json"))
		print url
		assert '{"name":"damien","age":8,"values":["toto","titi"],"objects":{"sub2":"titi","sub1":"toto"}}' == url
	}
	
	@Test
	void testPythonSerializationXML() {
		def input = ['o':null]
		String python = "o = { 'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(pyDataConverter.toMap(res.o), "application/xml"))
		print url
		assert '<root><values>toto</values><values>titi</values><objects><sub2>titi</sub2><sub1>toto</sub1></objects><name>damien</name><age>8</age></root>' == url
	}
	
	@Test
	void testPythonSerializationXMLRoot() {
		def input = ['o':null]
		String python = "o = { 'values':{'name':'damien','age':8, 'values':['toto','titi'], 'objects':{'sub1':'toto', 'sub2':'titi'}}}";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.bytes,"script"),input)
		def url = new String(dataSerializer.serialize(pyDataConverter.toMap(res.o), "application/xml"))
		print url
		assert '<values><values>toto</values><values>titi</values><objects><sub2>titi</sub2><sub1>toto</sub1></objects><name>damien</name><age>8</age></values>' == url
	}
	
	@Test
	void testPythonSerializationMinidom() {
		def input = ['document':null]
		String python = 
"""from xml.dom import *
acc = u'é'
document = getDOMImplementation().createDocument(EMPTY_NAMESPACE,'Album',None)
name = document.createElement('name')
name.appendChild(document.createTextNode('Pornography'))
band = document.createElement('band')
band.appendChild(document.createTextNode(u'The cure'))
bband = band.firstChild.nodeValue
document.documentElement.appendChild(name)
document.documentElement.appendChild(band)""";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.getBytes(),"script"),input)
		
		def bytes = dataSerializer.serialize(res.document, "application/xml")
		assert '<?xml version="1.0" encoding="UTF-8"?>\n<Album><name>Pornography</name><band>The cure</band></Album>' == new String(bytes,'utf-8')
	}
	
	@Test
	void testPythonSerializationMinidomtoxml() {
		def input = ['document':null]
		String python =
"""from xml.dom import *
acc = u'é'
document = getDOMImplementation().createDocument(EMPTY_NAMESPACE,'Album',None)
name = document.createElement('name')
name.appendChild(document.createTextNode('Pornography'))
band = document.createElement('band')
band.appendChild(document.createTextNode(u'The cure'))
bband = band.firstChild.nodeValue
document.documentElement.appendChild(name)
document.documentElement.appendChild(band)
document = document.toxml('utf-8').decode('utf-8')
""";
		def res = pythonScriptExecutor.execute(new be.solidx.hot.Script<CompiledScript>(python.getBytes(),"script"),input)
		
		def bytes = dataSerializer.serialize(res.document, "application/xml")
		println new String(bytes).trim()
		assert '<?xml version="1.0" encoding="utf-8"?>\n<Album><name>Pornography</name><band>The cure</band></Album>' == new String(bytes,'utf-8').trim()
	}
}
