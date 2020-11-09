package be.solidx.hot.test.shows;

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

import static org.junit.Assert.*

import groovy.transform.CompileStatic;

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

import org.junit.Test

import be.solidx.hot.groovy.GroovyScriptExecutor
import be.solidx.hot.js.JSScriptExecutor
import be.solidx.hot.python.PythonScriptExecutor
import be.solidx.hot.shows.groovy.GroovyShow


@CompileStatic
class TestTaskManager {

	GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor()
	PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor()
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor();
	
	@Test
	public void testTaskManager() {
		new File(System.getProperty("java.io.tmpdir")+"/delay.txt").delete()
		new File(System.getProperty("java.io.tmpdir")+"/cron.txt").delete()
		
		ExecutorService tmExecutorService = Executors.newSingleThreadExecutor()
		ScheduledExecutorService sec = Executors.newScheduledThreadPool(1);
		GroovyShow groovyShow = new GroovyShow(
			getClass().getResource("/task-manager-show.groovy"),
			Executors.newFixedThreadPool(1), 
			Executors.newFixedThreadPool(10),
			null,
			groovyScriptExecutor,
			sec, 
			null);
		
		sleep 10000
		
		assert new File(System.getProperty("java.io.tmpdir")+"/delay.txt").text == "delayed task"
		assert new File(System.getProperty("java.io.tmpdir")+"/cron.txt").text == "3"
		
		new File(System.getProperty("java.io.tmpdir")+"/delay.txt").delete()
		new File(System.getProperty("java.io.tmpdir")+"/cron.txt").delete()
	}

}
