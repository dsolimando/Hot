package be.solidx.hot.test.mongo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import be.solidx.hot.data.mongo.DBObjectNativeObjectTransformer;
import be.solidx.hot.data.mongo.js.DB;

import com.mongodb.Mongo;

public class TestMongoScripts {
	
	public static void main(String[] args) throws IOException {
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
		context.evaluateReader(scope, new InputStreamReader(TestMongoScripts.class.getResourceAsStream("/js/underscore.js")), "underscore", 1, null);
		Object result = context.evaluateReader(scope, new InputStreamReader(TestMongoScripts.class.getResourceAsStream("/mongo.js")), "test", 1, null);
		ObjectMapper objectMapper = new ObjectMapper ();
//		System.out.println(objectMapper.writeValueAsString(result));;
		printWriter.flush();
		Context.exit();
	}
}
