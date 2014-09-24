package be.icode.hot.test.data.jdbc.sql

import org.junit.Test;

import be.icode.hot.data.jdbc.sql.SelectQuery

class TestMysqlSelectQueryBuilder extends TestCommonSqlQueryBuilder {

	@Test
	void testRegex () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			name: [$regex: '[a-dXYZ]']
		])
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE sc1.employes.name REGEXP :sc1_employes_name_0"
	}
	
	@Test
	void testSelectMethod2() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT COUNT(*) FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.salary = :sc1_employes_salary_0 )" == selectQuery.count().build()
	}
	
	@Test
	void testSelectMethod3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT * FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.salary = :sc1_employes_salary_0 ) LIMIT 2" == selectQuery.limit(2).build()
	}
	
	@Test
	void testSelectMethod4() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT * FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.salary = :sc1_employes_salary_0 ) LIMIT 2 OFFSET 3" == selectQuery.limit(2).skip(3).build()
	}
}
