package be.solidx.hot.test.data.jdbc;

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

import be.solidx.hot.Script;
import be.solidx.hot.data.jdbc.js.DB;
import be.solidx.hot.data.jdbc.js.JSAsyncDB;
import be.solidx.hot.js.JSScriptExecutor;

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
		Script<org.mozilla.javascript.Script> script = new Script<>(IOUtils.toByteArray(getClass().getResourceAsStream("/be/solidx/hot/test/data/jdbc/scripts/async-db.js")), "async-db.js");
		StringWriter out = new StringWriter();
		executor.execute(script, context, out);
		System.out.println(out.toString());
		Assert.assertFalse(out.toString().contains("FAIL"));
	}
}
