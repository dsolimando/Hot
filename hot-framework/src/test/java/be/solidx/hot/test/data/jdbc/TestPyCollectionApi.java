package be.solidx.hot.test.data.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.script.CompiledScript;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.core.PyDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.solidx.hot.Script;
import be.solidx.hot.data.jdbc.DB;
import be.solidx.hot.data.jdbc.python.PyAsyncDB;
import be.solidx.hot.python.PythonScriptExecutor;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TestPyCollectionApi {

	@Autowired
	DB<PyDictionary> db;
	
	PyAsyncDB asyncDB;
	
	@PostConstruct
	public void init () {
		asyncDB = new PyAsyncDB(db, Executors.newCachedThreadPool(), Executors.newSingleThreadExecutor());
	}
	
	@Test
	public void testSync() throws Exception {
		Map<String, Object> context = new HashMap<>();
		context.put("db", db);
		Script<CompiledScript> script = new Script<>(
				IOUtils.toByteArray(getClass().getResourceAsStream("/be/solidx/hot/test/data/jdbc/scripts/db.py")), "py-db");
		PythonScriptExecutor executor = new PythonScriptExecutor();
		executor.execute(script, context);
	}
}
