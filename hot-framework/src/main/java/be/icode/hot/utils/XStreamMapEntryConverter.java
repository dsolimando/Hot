package be.icode.hot.utils;

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
