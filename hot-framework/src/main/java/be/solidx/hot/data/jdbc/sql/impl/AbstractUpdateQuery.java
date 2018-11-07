package be.solidx.hot.data.jdbc.sql.impl;

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;

abstract public class AbstractUpdateQuery extends AbstractQueryWithCriteria {

    public static final String $SET = "$set";

	private Map<String,Object> updateParameters = new LinkedHashMap<String, Object>();
	
	public AbstractUpdateQuery(TableMetadata tableMetadata, Map<String,Object> updateParameters, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
		if (updateParameters.containsKey($SET))
		    this.updateParameters.putAll((Map<? extends String, ?>) updateParameters.get($SET));
		else
		    this.updateParameters.putAll(fillWithNull(updateParameters));
	}

	@Override
	public String build() {
		String query = "UPDATE %s " + "SET %s" + "%s";
		String sets = "";
		String separator = "";
		
		for (String key : updateParameters.keySet()) {
			String keyCopy = key;
			if (keyCopy.split("\\.").length < 2) keyCopy = tableMetadata.getName() + "." + keyCopy;
			keyCopy = withSchema(keyCopy);
			sets += String.format("%s%s=:%s_%s", separator, key, keyCopy.replaceAll("\\.", "_"),criterionValues.get(keyCopy).size());
			separator = ", ";
			criterionValues.put(keyCopy, updateParameters.get(key));
		}
		return String.format(query, tablenameWithSchema(), sets, buildWhereClauses()).trim();
	}

    private Map<String,Object> fillWithNull (Map<String,Object> parameters) {
        Map<String,Object> filledMap = new HashMap<>(parameters);
	    for (String column: this.tableMetadata.getColumns()) {
	        if (!parameters.containsKey(column)) {
	            filledMap.put(column, null);
            }
        }
        return filledMap;
    }
}
