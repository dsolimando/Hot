package be.solidx.hot.spring.config;

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

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.xml.sax.SAXException;

import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.groovy.GroovyWebScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.js.JSWebScriptExecutor;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.python.PyDictionaryConverter;
import be.solidx.hot.python.PythonWebScriptExecutor;
import be.solidx.hot.utils.GroovyHttpDataDeserializer;
import be.solidx.hot.utils.JsHttpDataDeserializer;
import be.solidx.hot.utils.PythonHttpDataDeserializer;

@Configuration @Lazy
@Import({CommonConfig.class})
public class ScriptExecutorsConfig {

	@Autowired
	HotConfig hotConfig;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	GroovyMapConverter groovyDataConverter;
	
	@Autowired
	PyDictionaryConverter pyDictionaryConverter;
	
	@Autowired
	JsMapConverter jsDataConverter;
	
	@Bean
	public GroovyWebScriptExecutor groovyScriptExecutor () {
		GroovyWebScriptExecutor groovyScriptExecutor = new GroovyWebScriptExecutor();
		groovyScriptExecutor.setDevMode(hotConfig.isDevMode());
		return groovyScriptExecutor;
	}
	
	@Bean
	public PythonWebScriptExecutor pythonScriptExecutorWithPreExecuteScripts () {
		PythonWebScriptExecutor pythonWebScriptExecutor = new PythonWebScriptExecutor();//Arrays.asList("/be/icode/hot/rest/scripts/pyRestControllerInit.py"));
		pythonWebScriptExecutor.setDevMode(hotConfig.isDevMode());
		return pythonWebScriptExecutor;
	}
	
	@Bean
	public JSWebScriptExecutor jSScriptExecutorWithPreExecuteScripts () {
		JSWebScriptExecutor jsScriptExecutor = new JSWebScriptExecutor();
		jsScriptExecutor.setInterpretive(true);
		jsScriptExecutor.setDevMode(hotConfig.isDevMode());
		jsScriptExecutor.setGlobalScopeScripts(Arrays.asList("/js/backbone.js"));
		return jsScriptExecutor;
	}
	
	@Bean
	public GroovyHttpDataDeserializer groovyHttpDataDeserializer() throws JsonParseException, JsonMappingException, ParserConfigurationException, SAXException, IOException {
		return new GroovyHttpDataDeserializer(objectMapper);
	}
	
	@Bean
	public JsHttpDataDeserializer jsHttpDataDeserializer() {
		return new JsHttpDataDeserializer(objectMapper, jsDataConverter, jSScriptExecutorWithPreExecuteScripts());
	}
	
	@Bean
	public PythonHttpDataDeserializer pythonHttpDataDeserializer() throws ParserConfigurationException {
		return new PythonHttpDataDeserializer(objectMapper, pyDictionaryConverter);
	}
}
