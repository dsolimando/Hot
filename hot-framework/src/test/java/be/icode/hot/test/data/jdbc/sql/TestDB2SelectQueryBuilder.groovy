package be.icode.hot.test.data.jdbc.sql;

import org.junit.Test;

import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory
import be.icode.hot.data.jdbc.sql.SelectQuery
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine


public class TestDB2SelectQueryBuilder extends TestCommonSqlQueryBuilder {

	QueryBuilder queryBuilder = QueryBuilderFactory.buildQueryBuilder(DBEngine.DB2)
	
	@Test
	void testSelectMethod3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT COUNT(*) FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.salary = :sc1_employes_salary_0 )" == selectQuery.count().limit(2).build()
	}
	
	@Test
	void testSelectMethod4() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM "+
					"(SELECT ROW_NUMBER() OVER (ORDER BY sc1.employes.id ASC) AS rn, sc1.employes.* "+
					"FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.age = :sc1_employes_age_0 ))"+
					" WHERE rn > 3 AND rn <= 5 "
	}
	
	@Test
	void testSelectMethod5() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		selectQuery.addJoins joinTree
		print selectQuery.limit(2).skip(3).build()
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM "+
				"(SELECT ROW_NUMBER() OVER (ORDER BY sc1.employes.id ASC) AS rn, "+
				"sc1.classes.id AS classes_id, sc1.classes.floor AS classes_floor, sc1.classes.name AS classes_name, sc1.personnes.nom AS personnes_nom, "+
				"sc1.personnes.prenom AS personnes_prenom, sc1.personnes.age AS personnes_age, sc1.personnes.adresse AS personnes_adresse, sc1.personnes.classe_id AS personnes_classe_id, sc1.personnes.classe_floor AS personnes_classe_floor, sc1.personnes.network_id AS personnes_network_id, "+
				"sc1.enfants.nom AS enfants_nom, sc1.enfants.prenom AS enfants_prenom, sc1.enfants.age AS enfants_age, sc1.enfants.personne_nom AS enfants_personne_nom, sc1.enfants.personne_prenom AS enfants_personne_prenom, "+
				"sc1.voitures.marque AS voitures_marque, sc1.voitures.modele_id AS voitures_modele_id, sc1.voitures.couleur AS voitures_couleur, sc1.voitures.personne_nom AS voitures_personne_nom, sc1.voitures.personne_prenom AS voitures_personne_prenom "+
				"FROM sc1.classes "+
				"LEFT OUTER JOIN sc1.personnes ON sc1.personnes.classe_id = sc1.classes.id AND sc1.personnes.classe_floor = sc1.classes.floor "+
				"LEFT OUTER JOIN sc1.enfants ON sc1.enfants.personne_nom = sc1.personnes.nom AND sc1.enfants.personne_prenom = sc1.personnes.prenom "+
				"LEFT OUTER JOIN sc1.voitures ON sc1.voitures.personne_nom = sc1.personnes.nom AND sc1.voitures.personne_prenom = sc1.personnes.prenom "+
				"WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.age = :sc1_employes_age_0 )) "+
				"WHERE rn > 3 AND rn <= 5 "
	}
	
	@Test
	void testSelectMethod6() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		print selectQuery.limit(2).build()
		assert selectQuery.limit(2).build() == "SELECT * FROM sc1.employes WHERE ( sc1.employes.position = :sc1_employes_position_0 AND sc1.employes.age = :sc1_employes_age_0 ) FETCH FIRST 2 ROWS ONLY"
	}
}
