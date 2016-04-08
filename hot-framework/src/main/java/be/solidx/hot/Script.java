package be.solidx.hot;

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

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;

import be.solidx.hot.exceptions.ScriptException;
import be.solidx.hot.utils.Cacheable;

public class Script<COMPILED_SCRIPT> implements Cacheable<String>{

	private byte[] code;

	private String md5;

	private String name;
	
	private String encoding;
	
	private COMPILED_SCRIPT compiledScript;
	
	public Script(byte[] code, String name) {
		this.code = code;
		this.name = name;
		md5 = DigestUtils.md5Hex(code);
	}
	
	public byte[] getCode() {
		return code;
	}
	
	public String getCodeUTF8 () throws ScriptException {
		try {
			return new String (code,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ScriptException("Unsupported charset");
		}
	}
	
	public String getCodeISO88591 () throws ScriptException {
		try {
			return new String (code,"ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new ScriptException("Unsupported charset");
		}
	}
	
	public void setCode(byte[] code) {
		this.code = code;
		md5 = DigestUtils.md5Hex(code);
	}

	public String getName() {
		return name;
	}
	
	public String getMd5() {
		return md5;
	}
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public void setCompiledScript(COMPILED_SCRIPT compiledScript) {
		this.compiledScript = compiledScript;
	}
	
	public COMPILED_SCRIPT getCompiledScript() {
		return compiledScript;
	}

	@Override
	public String getValue() {
		try {
			return new String(getCode(),"UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean outdated(String value) {
		String md5 = DigestUtils.md5Hex(value.getBytes());
		return !md5.equals(this.md5);
	}
	
	@Override
	public String getId() {
		return getName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Script) {
			Script<COMPILED_SCRIPT> toCompare = (Script<COMPILED_SCRIPT>) obj;
			return toCompare.md5.equals(md5);
		}
		return false;
	}
}
