package be.solidx.hot.data.jdbc.sql.criterion;

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

import be.solidx.hot.data.criterion.Operator;


public class MultivaluedCriterion extends CriterionImpl {
	
	private static final String CRITERION_PATTERN = "%s %s (%s)";
	private static final String MULTIVALUED_IN_PATTERN = "%s :%s_%d";

	private String parameterNameList = "";
	
	public MultivaluedCriterion(Operator operator, String parameterName, int index, int numParameters) {
		super(operator, parameterName, index);
		String separator = "";
		for (int i = 0; i < numParameters; i++) {
			parameterNameList += String.format(MULTIVALUED_IN_PATTERN, separator,getSubstitutionParameterName(),index+i);
			separator = ",";
		}
	}

	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName,operator,parameterNameList);
	}
}
