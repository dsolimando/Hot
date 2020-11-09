package be.solidx.hot.test.data.jdbc;

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
