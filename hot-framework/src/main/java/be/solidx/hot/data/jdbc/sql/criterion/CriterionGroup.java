package be.solidx.hot.data.jdbc.sql.criterion;

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

import java.util.ArrayList;
import java.util.List;

import be.solidx.hot.data.criterion.Criterion;

public class CriterionGroup implements Criterion {

	private boolean and = true;
	
	private List<Criterion> criteria = new ArrayList<Criterion>();

	public CriterionGroup(boolean and, List<? extends Criterion> whereClauses) {
		this.and = and;
		this.criteria.addAll(whereClauses);
	}
	
	@Override
	public String toString() {
		String clause = "";
		if (criteria.size() > 1) clause += "(";
		String separator = "";
		for (Criterion criterion : criteria) {
			clause += String.format("%s %s", separator,criterion);
			separator = and?" AND":" OR";
		}
		if (criteria.size() > 1) clause += " )";
		return clause.trim();
	}
}
