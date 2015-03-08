package be.solidx.hot.test.data.jdbc.sql

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
