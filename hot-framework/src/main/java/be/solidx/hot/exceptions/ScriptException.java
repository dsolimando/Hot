package be.solidx.hot.exceptions;

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

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = -6053764149659622124L;

	public ScriptException() {
	}

	public ScriptException(String arg0) {
		super(arg0);
	}

	public ScriptException(Throwable arg0) {
		super(arg0);
	}

	public ScriptException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
