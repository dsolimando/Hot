package be.solidx.hot.shows.spring;

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

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import be.solidx.hot.shows.ClosureRequestMapping;

import com.google.common.net.HttpHeaders;

public class RequestMappingMvcRequestMappingAdapter {
	
//	private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toLowerCase();
//	private static final String ACCEPT = HttpHeaders.ACCEPT.toLowerCase();
	
	RequestMappingInfo getRequestMappingInfo (final ClosureRequestMapping requestMapping) {
		
		List<String> consumeConditions = new ArrayList<>();
		List<String> produceConditions = new ArrayList<>();
		
		List<String> nameValueHeaders = new ArrayList<>();
		
		for (String header : requestMapping.getHeaders()) {
			nameValueHeaders.add(header.replaceFirst(":", "="));
			if (header.contains(HttpHeaders.CONTENT_TYPE)) {
//				consumeConditions.add(HttpHeaders.CONTENT_TYPE+":"+ header.split(":")[1]);
				consumeConditions.add(header.split(":")[1].trim());
			}
			if (header.contains(HttpHeaders.ACCEPT)) {
				produceConditions.add(header.split(":")[1].trim());
//				produceConditions.add(HttpHeaders.ACCEPT+":"+ header.split(":")[1]);
			}
		}
		
		String[] headers = requestMapping.getHeaders().toArray(new String[]{});
		
		return new RequestMappingInfo(
			new PatternsRequestCondition(requestMapping.getPaths().toArray(new String[]{})), 
			new RequestMethodsRequestCondition(requestMapping.getRequestMethod()), 
			new ParamsRequestCondition(requestMapping.getParams().toArray(new String[]{})), 
			new HeadersRequestCondition(nameValueHeaders.toArray(new String[]{})), 
			new ConsumesRequestCondition(consumeConditions.toArray(new String[]{}),headers), 
			new ProducesRequestCondition(produceConditions.toArray(new String[]{}),headers),
			null);
	}
}
