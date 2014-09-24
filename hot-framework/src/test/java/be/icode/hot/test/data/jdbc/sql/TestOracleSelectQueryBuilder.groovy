package be.icode.hot.test.data.jdbc.sql

import org.junit.Test;

import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory
import be.icode.hot.data.jdbc.sql.SelectQuery
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine

class TestOracleSelectQueryBuilder extends TestDB2SelectQueryBuilder {

	QueryBuilder queryBuilder = QueryBuilderFactory.buildQueryBuilder(DBEngine.ORACLE);
	
		@Test
		void testRegex () {
			SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
			selectQuery.addWhereClauses([
						name: [$regex: '[a-dXYZ]']
					])
			assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE REGEXP_LIKE (sc1.employes.name,:sc1_employes_name_0)"
		}
		
		@Test
		void testSelectMethod4() {
			SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
			selectQuery.addWhereClauses([position:"chief", age:"10"])
			assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM ( "+
				"SELECT a.*, ROWNUM rn FROM ( SELECT * FROM sc1.employes "+
				"WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.age = :sc1_employes_age_0 ) ) a "+
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
					"SELECT * FROM sc1.classes "+
						"LEFT OUTER JOIN sc1.personnes ON sc1.personnes.classe_id = sc1.classes.id AND sc1.personnes.classe_floor = sc1.classes.floor "+
						"LEFT OUTER JOIN sc1.enfants ON sc1.enfants.personne_nom = sc1.personnes.nom AND sc1.enfants.personne_prenom = sc1.personnes.prenom "+
						"LEFT OUTER JOIN sc1.voitures ON sc1.voitures.personne_nom = sc1.personnes.nom AND sc1.voitures.personne_prenom = sc1.personnes.prenom "+
					"WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.age = :sc1_employes_age_0 ) ) "+
				"a WHERE ROWNUM <= 5  ) "+
			"WHERE rn > 3"
		}
	
		@Test
		void testSelectMethod6() {
			SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
			selectQuery.addWhereClauses([position:"chief", salary:"10000"])
			assert "SELECT * FROM ( SELECT * FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.salary = :sc1_employes_salary_0 ) ) WHERE ROWNUM <= 3" == selectQuery.limit(3).build()
		}
}
