package be.solidx.hot.data.jdbc.sql.impl;

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
