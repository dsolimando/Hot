package be.solidx.hot.exceptions;

import java.util.Arrays;
import java.util.Collections;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import com.google.common.base.Joiner;

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

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = -6053764149659622124L;
	
	public ScriptException() {
	}

	public ScriptException(String sourceFilename) {
		super(sourceFilename);
	}

	public ScriptException(Throwable scriptException) {
		super(scriptException);
	}

	public ScriptException(String sourceFilename, Throwable scriptException) {
		super(sourceFilename, scriptException);
	}
	
	@Override
	public String getMessage() {
		if (getCause() instanceof javax.script.ScriptException) {
			javax.script.ScriptException se = (javax.script.ScriptException) getCause();
			if (se.getCause() instanceof MultipleCompilationErrorsException) {
				String[] splitted = se.getMessage().split(":");
				if (splitted.length > 3) {
					return super.getMessage() + ":" + Joiner.on(":").join(Arrays.copyOfRange(splitted, 3, splitted.length));
				}
			}
			return Joiner.on(":").join(super.getMessage(), se.getMessage());
		}
		else return getCause().getMessage();
	}
}
