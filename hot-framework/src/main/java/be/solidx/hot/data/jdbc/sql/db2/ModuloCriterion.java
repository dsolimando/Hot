package be.solidx.hot.data.jdbc.sql.db2;

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
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;

public class ModuloCriterion extends CriterionImpl {
	
	private static final String CRITERION_PATTERN = "MOD(%s,:%s_%d) = :%s_%d";

	public ModuloCriterion(String parameterName, int index) {
		super(Operator.$mod, parameterName, index);
	}

	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName,substitutionParameterName,index,substitutionParameterName,index+1);
	}
}
