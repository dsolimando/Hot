package be.solidx.hot.test.mongo;

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
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Mongo;

import be.solidx.hot.data.mongo.DBObjectNativeObjectTransformer;
import be.solidx.hot.data.mongo.js.DB;

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
