package be.solidx.hot.shows.groovy;

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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

import be.solidx.hot.shows.ClosureRequestMapping.Options;
import be.solidx.hot.shows.RestRequest;
import be.solidx.hot.utils.HttpDataDeserializer;
import be.solidx.hot.utils.ScriptMapConverter;

public class GroovyRestRequest extends RestRequest<Map<?, ?>> {
	
	public GroovyRestRequest(
			Options options, 
			ScriptMapConverter<Map<?, ?>> scriptMapConverter, 
			HttpDataDeserializer httpDataDeserializer,
			HttpServletRequest httpServletRequest,
			byte[] body,
			Authentication authentication) {
		super(options, scriptMapConverter, httpDataDeserializer, httpServletRequest, body);
		initUser(authentication);
	}
	
	@Override
	public Map<?, ?> getUser() {
		return userAsMap;
	}
}
