package be.solidx.hot.web.deprecated;

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

import java.io.Writer;
import java.util.Map;

import org.springframework.web.context.request.WebRequest;

import be.solidx.hot.Script;
import be.solidx.hot.ScriptExecutor;
import be.solidx.hot.exceptions.ScriptException;

public interface WebScriptExecutor<COMPILED_SCRIPT,DB> extends ScriptExecutor<COMPILED_SCRIPT> {

	Object execute(Script<COMPILED_SCRIPT> script, WebRequest webRequest, Map<String, DB> dbMap) throws ScriptException;

	Object execute(Script<COMPILED_SCRIPT> script, WebRequest webRequest, Map<String, DB> dbMap, Writer writer) throws ScriptException;
}
