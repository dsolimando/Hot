package be.icode.hot.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import be.icode.hot.utils.FileLoader;
import be.icode.hot.web.TranspiledScriptsController;

@Configuration
@EnableWebMvc
public class ControllersConfig {

	@Autowired
	DataConfig dataConfig;
	
	@Autowired
	ScriptExecutorsConfig scriptConfig;
	
	@Autowired
	HotConfig hotConfig;
	
	@Autowired
	ThreadPoolsConfig threadPoolsConfig;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Bean
	public FileLoader fileLoader() throws Exception {
		return new FileLoader(threadPoolsConfig.staticResourcesEventLoop());
	}
	
	@Bean
	public TranspiledScriptsController staticResourcesController () throws Exception {
		return new TranspiledScriptsController(
				fileLoader(), 
				threadPoolsConfig.blockingTasksThreadPool(), 
				threadPoolsConfig.staticResourcesEventLoop(), 
				scriptConfig.lessCompiler(),
				scriptConfig.coffeeScriptCompiler(),
				hotConfig.isDevMode());
	}
	
	
	
	
//	@Bean
//	public GroovyPageCompiler groovyPageCompiler () throws JsonParseException, JsonMappingException, IOException {
//		GroovyPageCompiler groovyPageCompiler = new GroovyPageCompiler();
//		groovyPageCompiler.setDevMode(hotConfig.isDevMode());
//		return groovyPageCompiler;
//	}
//	
//	@Bean
//	public GroovyController groovyController () throws Exception {
//		GroovyController controller = new GroovyController();
//		controller.setDbMap(dataConfig.groovyDbMap());
//		controller.setPageCompiler(groovyPageCompiler());
//		controller.setScriptExecutor(scriptConfig.groovyScriptExecutor());
//		return controller;
//	}
//	
//	@Bean
//	public JSPageCompiler jsPageCompiler () throws JsonParseException, JsonMappingException, IOException {
//		JSPageCompiler jsPageCompiler = new JSPageCompiler();
//		jsPageCompiler.setDevMode(hotConfig.isDevMode());
//		return jsPageCompiler;
//	}
//	
//	@Bean
//	public JSController jsController () throws Exception {
//		JSController jsController = new JSController();
//		jsController.setDbMap(dataConfig.jsDbMap());
//		jsController.setPageCompiler(jsPageCompiler());
//		jsController.setScriptExecutor(scriptConfig.jSScriptExecutorWithPreExecuteScripts());
//		return jsController;
//	}
//	
//	@Bean
//	public PythonPageCompiler pythonPageCompiler() throws JsonParseException, JsonMappingException, IOException {
//		PythonPageCompiler pythonPageCompiler = new PythonPageCompiler();
//		pythonPageCompiler.setDevMode(hotConfig.isDevMode());
//		return pythonPageCompiler;
//	}
//	
//	@Bean
//	public PythonController pythonController () throws Exception {
//		PythonController pythonController = new PythonController();
//		pythonController.setDbMap(dataConfig.pythonDbMap());
//		pythonController.setPageCompiler(pythonPageCompiler());
//		pythonController.setScriptExecutor(scriptConfig.pythonScriptExecutorWithPreExecuteScripts());
//		return pythonController;
//	}
//	
//	@Bean
//	public WebResourcesController webResourcesController () {
//		WebResourcesController webResourcesController = new WebResourcesController();
//		webResourcesController.setCoffeeScriptCompiler(scriptConfig.coffeeScriptCompiler());
//		webResourcesController.setLessCompiler(scriptConfig.lessCompiler());
//		return webResourcesController;
//	}
//	
//	@Bean
//	public JspController jspController() {
//		return new JspController();
//	}
}
