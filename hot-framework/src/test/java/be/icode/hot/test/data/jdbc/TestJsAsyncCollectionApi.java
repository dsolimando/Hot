package be.icode.hot.test.data.jdbc;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.icode.hot.Script;
import be.icode.hot.data.jdbc.js.DB;
import be.icode.hot.data.jdbc.js.JSAsyncDB;
import be.icode.hot.js.JSScriptExecutor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestJsAsyncCollectionApi {

	@Autowired
	DB db;
	
	JSAsyncDB asyncDB;
	
	@Test
	public void testAsync() throws Exception {
		JSScriptExecutor executor = new JSScriptExecutor();
		Map<String, Object> context = new HashMap<>();
		context.put("db", db);
		context.put("adb",  new JSAsyncDB(db, Executors.newCachedThreadPool(), Executors.newSingleThreadExecutor(), executor.getGlobalScope()));
		
		executor.setGlobalScopeScripts(Arrays.asList("/js/qunit-1.14.js"));
		Script<org.mozilla.javascript.Script> script = new Script<>(IOUtils.toByteArray(getClass().getResourceAsStream("/be/icode/hot/test/data/jdbc/scripts/async-db.js")), "async-db.js");
		StringWriter out = new StringWriter();
		executor.execute(script, context, out);
		System.out.println(out.toString());
		Assert.assertFalse(out.toString().contains("FAIL"));
	}
}
