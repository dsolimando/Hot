package be.solidx.hot.data.jdbc.sql.impl;

import java.util.List;

import be.solidx.hot.data.jdbc.TableMetadata;

public class InsertQuery extends AbstractQuery {

	List<String> valuesParameters;
	
	public InsertQuery(TableMetadata tableMetadata, List<String> valuesParameters) {
		super(tableMetadata);
		this.valuesParameters = valuesParameters;
	}

	@Override
	public String build() {
		String separator = "";
		String names = "";
		String values = "";
		for (String key : valuesParameters) {
			names += (separator + key);
			values += (separator + ":"+key);
			separator = ",";
		}
		String queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", tablenameWithSchema(),names,values).trim();
		System.out.println(queryString);
		return queryString;
	}

}
