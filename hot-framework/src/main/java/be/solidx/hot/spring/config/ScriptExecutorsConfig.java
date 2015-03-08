package be.solidx.hot.spring.config;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.xml.sax.SAXException;

import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.groovy.GroovyWebScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.js.JSWebScriptExecutor;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.js.transpilers.CoffeeScriptCompiler;
import be.solidx.hot.js.transpilers.LessCompiler;
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
		jsScriptExecutor.setDevMode(hotConfig.isDevMode());
		return jsScriptExecutor;
	}
	
	@Bean
	public JSScriptExecutor coffeescriptCompilerScriptExecutor () {
		JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
		jsScriptExecutor.setDevMode(hotConfig.isDevMode());
		jsScriptExecutor.setInterpretive(true);
		jsScriptExecutor.setGlobalScopeScripts(Arrays.asList("/js/coffee-script.js","/js/less.js"));
		return jsScriptExecutor;
	}
	
	@Bean
	public CoffeeScriptCompiler coffeeScriptCompiler () {
		CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler();
		coffeeScriptCompiler.setDevMode(hotConfig.isDevMode());
		coffeeScriptCompiler.setJsScriptExecutor(coffeescriptCompilerScriptExecutor());
		return coffeeScriptCompiler;
	}
	
	@Bean
	public LessCompiler lessCompiler () {
		LessCompiler lessCompiler = new LessCompiler();
		lessCompiler.setDevMode(hotConfig.isDevMode());
		lessCompiler.setJsScriptExecutor(coffeescriptCompilerScriptExecutor());
		return lessCompiler;
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
