package be.solidx.hot.test.data.jdbc.sql

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


import org.junit.Test

import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.SelectQuery;

class TestMysqlSelectQueryBuilderNoSchema extends TestCommonSqlQueryBuilderNoSchema {

	@Test
	void testRegex () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			name: [$regex: '[a-dXYZ]']
		])
		assert selectQuery.build() == "SELECT * FROM employes WHERE employes.name REGEXP :employes_name_0"
	}
	
	@Test
	void testSelectMethod2() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT COUNT(*) FROM employes WHERE ( employes.position = :employes_position_0 AND employes.salary = :employes_salary_0 )" == selectQuery.count().build()
	}
	
	@Test
	void testSelectMethod3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT * FROM employes WHERE ( employes.position = :employes_position_0 AND employes.salary = :employes_salary_0 ) LIMIT 2" == selectQuery.limit(2).build()
	}
	
	@Test
	void testSelectMethod4() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT * FROM employes WHERE ( employes.position = :employes_position_0 AND employes.salary = :employes_salary_0 ) LIMIT 2 OFFSET 3" == selectQuery.limit(2).skip(3).build()
	}
}
