package be.icode.hot.data.jdbc.sql.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;

abstract public class AbstractUpdateQuery extends AbstractQueryWithCriteria {

	private Map<String,Object> updateParameters = new LinkedHashMap<String, Object>();
	
	public AbstractUpdateQuery(TableMetadata tableMetadata, Map<String,Object> updateParameters, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
		this.updateParameters.putAll(updateParameters);
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
			sets += String.format("%s%s=:%s_%s", separator, keyCopy, keyCopy.replaceAll("\\.", "_"),criterionValues.get(keyCopy).size());
			separator = ", ";
			criterionValues.put(keyCopy, updateParameters.get(key));
		}
		String queryString = String.format(query, tablenameWithSchema(), sets, buildWhereClauses()).trim();
		System.out.println(queryString);
		return queryString;
	}
}
