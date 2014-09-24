package be.icode.hot.test.mongo;

import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import be.icode.hot.data.mongo.DBObjectNativeObjectTransformer;
import be.icode.hot.data.mongo.js.DB;

import com.mongodb.Mongo;

public class TestMongoScripts {
	
//	@Test
//	public void testPython() throws Exception {
//		ScriptEngineManager engineManager = new ScriptEngineManager();
//		ScriptEngine engine = engineManager.getEngineByName("jython");
//		engine.eval(new InputStreamReader(getClass().getResourceAsStream("/mongo.py")));
//	}
	@Test
	public void testJs() throws Exception {
		Context context = Context.enter();
		ScriptableObject globalScope = context.initStandardObjects();
		Scriptable scope = context.newObject(globalScope);
		PrintWriter printWriter = new PrintWriter(System.out);
		ScriptableObject.putProperty(scope, "out", printWriter);
		DB db = new DB(					
				"", 
				"", 
				"TestMongoTemplat", 
				new Mongo(), 
				new DBObjectNativeObjectTransformer());
		ScriptableObject.putProperty(scope, "db", db);
		context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("/js/underscore.js")), "underscore", 1, null);
		Object result = context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("/mongo.js")), "test", 1, null);
		ObjectMapper objectMapper = new ObjectMapper ();
//		System.out.println(objectMapper.writeValueAsString(result));;
		printWriter.flush();
		Context.exit();
	}
}
