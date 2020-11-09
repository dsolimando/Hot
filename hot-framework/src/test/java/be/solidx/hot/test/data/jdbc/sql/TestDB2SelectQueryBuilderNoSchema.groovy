package be.solidx.hot.test.data.jdbc.sql

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
 
import org.junit.Test

import be.solidx.hot.data.jdbc.TableMetadata;

import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

class TestDB2SelectQueryBuilderNoSchema extends TestCommonSqlQueryBuilderNoSchema {

	QueryBuilder queryBuilder = QueryBuilderFactory.buildQueryBuilder(DBEngine.DB2)
	
	@Test
	void testSelectMethod3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", salary:"10000"])
		assert "SELECT COUNT(*) FROM employes WHERE ( employes.position = :employes_position_0 AND employes.salary = :employes_salary_0 )" == selectQuery.count().limit(2).build()
	}
	
	@Test
	void testSelectMethod4() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM "+
					"(SELECT ROW_NUMBER() OVER (ORDER BY employes.id ASC) AS rn, employes.* "+
					"FROM employes WHERE ( employes.position = :employes_position_0 AND employes.age = :employes_age_0 ))"+
					" WHERE rn > 3 AND rn <= 5 "
	}
	
	@Test
	void testSelectMethod5() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		selectQuery.addJoins joinTree
		assert selectQuery.limit(2).skip(3).build() == "SELECT * FROM "+
				"(SELECT ROW_NUMBER() OVER (ORDER BY employes.id ASC) AS rn, "+
				"classes.id AS classes_id, classes.floor AS classes_floor, classes.name AS classes_name, personnes.nom AS personnes_nom, "+
				"personnes.prenom AS personnes_prenom, personnes.age AS personnes_age, personnes.adresse AS personnes_adresse, personnes.classe_id AS personnes_classe_id, personnes.classe_floor AS personnes_classe_floor, personnes.network_id AS personnes_network_id, "+
				"enfants.nom AS enfants_nom, enfants.prenom AS enfants_prenom, enfants.age AS enfants_age, enfants.personne_nom AS enfants_personne_nom, enfants.personne_prenom AS enfants_personne_prenom, "+
				"voitures.marque AS voitures_marque, voitures.modele_id AS voitures_modele_id, voitures.couleur AS voitures_couleur, voitures.personne_nom AS voitures_personne_nom, voitures.personne_prenom AS voitures_personne_prenom "+
				"FROM classes "+
				"LEFT OUTER JOIN personnes ON personnes.classe_id = classes.id AND personnes.classe_floor = classes.floor "+
				"LEFT OUTER JOIN enfants ON enfants.personne_nom = personnes.nom AND enfants.personne_prenom = personnes.prenom "+
				"LEFT OUTER JOIN voitures ON voitures.personne_nom = personnes.nom AND voitures.personne_prenom = personnes.prenom "+
				"WHERE ( employes.position = :employes_position_0 AND employes.age = :employes_age_0 )) "+
				"WHERE rn > 3 AND rn <= 5 "
	}
	
	@Test
	void testSelectMethod6() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([position:"chief", age:"10"])
		print selectQuery.limit(2).build()
		assert selectQuery.limit(2).build() == "SELECT * FROM employes WHERE ( employes.position = :employes_position_0 AND employes.age = :employes_age_0 ) FETCH FIRST 2 ROWS ONLY"
	}
}
