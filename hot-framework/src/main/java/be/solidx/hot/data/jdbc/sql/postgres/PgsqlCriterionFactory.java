package be.solidx.hot.data.jdbc.sql.postgres;

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

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.jdbc.AbstractSqlCriterionFactory;

public class PgsqlCriterionFactory extends AbstractSqlCriterionFactory {

	@Override
	protected Criterion buildRegexpCriterion(String parameterName, String value, int index) {
		return new RegexpCriterion(parameterName, index);
	}

}
