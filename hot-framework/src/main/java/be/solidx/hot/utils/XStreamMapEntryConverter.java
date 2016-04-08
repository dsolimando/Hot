package be.solidx.hot.utils;

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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeObject;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class XStreamMapEntryConverter implements Converter {

    public boolean canConvert(Class clazz) {
        return AbstractMap.class.isAssignableFrom(clazz);
    }

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

        Map map = (Map) value;
        for (Object obj : map.entrySet()) {
        	if (!(map instanceof AbstractMap || map instanceof NativeObject))  continue;
            Map.Entry entry = (Map.Entry) obj;
            if (entry.getValue() instanceof Map) {
            	writer.startNode(entry.getKey().toString());
            	marshal(entry.getValue(), writer, context);
            	writer.endNode();
            } else if (entry.getValue() instanceof List){
            	for (Object listEntry : (List)entry.getValue()) {
            		Map<Object, Object> listToMap = new HashMap<>();
					listToMap.put(entry.getKey(), listEntry);
					marshal(listToMap, writer, context);
				}
            } else {
            	writer.startNode(entry.getKey().toString());
            	writer.setValue(entry.getValue().toString());
            	writer.endNode();
            }
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
       throw new RuntimeException("Not implemented");
    }
}
