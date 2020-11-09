package be.solidx.hot.utils;

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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

public interface ScriptMapConverter<MAP> {

	MAP toScriptMap(Map<?, ?> map);
	
	Map<?, ?> toMap(MAP map);
	
	MAP multiValueMapToMapList(Map<String, MultiValueMap<String, String>> matrixVariables);

	MAP httpHeadersToMap(WebRequest webRequest);

	MAP httpHeadersToMap(HttpServletRequest webRequest);
}
