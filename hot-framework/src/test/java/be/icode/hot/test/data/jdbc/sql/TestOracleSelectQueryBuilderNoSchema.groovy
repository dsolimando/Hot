package be.icode.hot.test.data.jdbc.sql

import org.junit.Test;

import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory
import be.icode.hot.data.jdbc.sql.SelectQuery
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine


class TestOracleSelectQueryBuilderNoSchema extends TestDB2SelectQueryBuilderNoSchema {

	QueryBuilder queryBuilder = QueryBuilderFactory.buildQueryBuilder(DBEngine.ORACLE);

	@Test
	void testRegex () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
					name: [$regex: '[a-dXYZ]']
				])
		assert selectQuery.build() == "SELECT * FROM employes WHERE REGEXP_LIKE (employes.name,:employes_name_0)"
	}

	@Test
	void testSelectMethod4() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM ( "+
			"SELECT a.*, ROWNUM rn FROM ( SELECT * FROM employes "+
			"WHERE ( employes.position = :employes_position_0 AND employes.age = :employes_age_0 ) ) a "+
			"WHERE ROWNUM <= 5  ) "+
			"WHERE rn > 3"
	}
	
	@Test
	void testSelectMethod5() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		selectQuery.addJoins joinTree
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM ( "+
			"SELECT a.*, ROWNUM rn FROM ( "+
				"SELECT * FROM classes "+
					"LEFT OUTER JOIN personnes ON personnes.classe_id = classes.id AND personnes.classe_floor = classes.floor "+
					"LEFT OUTER JOIN enfants ON enfants.personne_nom = personnes.nom AND enfants.personne_prenom = personnes.prenom "+
					"LEFT OUTER JOIN voitures ON voitures.personne_nom = personnes.nom AND voitures.personne_prenom = personnes.prenom "+
				"WHERE ( employes.position = :employes_position_0 AND employes.age = :employes_age_0 ) ) "+
			"a WHERE ROWNUM <= 5  ) "+
		"WHERE rn > 3"
	}
	
	@Test
	void testSelectMethod6() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT * FROM ( SELECT * FROM employes WHERE ( employes.position = :employes_position_0 AND employes.salary = :employes_salary_0 ) ) WHERE ROWNUM <= 3" == selectQuery.limit(3).build()
	}
}
