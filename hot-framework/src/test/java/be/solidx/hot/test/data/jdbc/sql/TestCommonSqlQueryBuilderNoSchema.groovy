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
 
import org.junit.Before
import org.junit.Test

import be.solidx.hot.data.jdbc.TableMetadata
import be.solidx.hot.data.jdbc.TableMetadata.ForeignKeySet
import be.solidx.hot.data.jdbc.groovy.JoinTree
import be.solidx.hot.data.jdbc.sql.Query
import be.solidx.hot.data.jdbc.sql.QueryBuilder
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria
import be.solidx.hot.data.jdbc.sql.SelectQuery
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine


class TestCommonSqlQueryBuilderNoSchema {

	TableMetadata classesMetadata
	TableMetadata personnesMetadata
	TableMetadata enfantsMetadata
	TableMetadata voituresMetadata
	TableMetadata employeMetadata
	
	JoinTree joinTree
	
	QueryBuilder queryBuilder = QueryBuilderFactory.buildQueryBuilder(DBEngine.H2)

	@Before
	void init () {
		classesMetadata = new TableMetadata(
			"classes", 	// Table name
			null,		// Schema
			["id", "floor", "name"], // Columns
			["id", "floor"],		// Primary Keys
			null,					// Foreign Keys
			[						// ExportedForeign Keys
				personnes: new ForeignKeySet(null,"personnes", ["id", "floor"], ["classe_id", "classe_floor"])
			]
		)

		personnesMetadata = new TableMetadata(
			"personnes",
			null,
			["nom","prenom","age","adresse","classe_id", "classe_floor","network_id"],
			["nom", "prenom"],
			[
				classes	:	new ForeignKeySet(null, "classes", ["id", "floor"], ["classe_id", "classe_floor"]),
				networks: 	new ForeignKeySet(null, "classes", ["id"], ["network_id"])
			],
			[
				enfants: 	new ForeignKeySet(null,"enfants", ["nom", "prenom"], ["personne_nom","presonne_prenom"]),
				voitures:	new ForeignKeySet(null,"voitures", ["nom", "prenom"], ["personne_nom","presonne_prenom"])
			]
		)

		enfantsMetadata = new TableMetadata(
			"enfants",
			null,
			["nom", "prenom", "age","personne_nom","personne_prenom"],
			["nom", "prenom"],
			[
				personnes:	new ForeignKeySet(null, "personnes", ["nom","prenom"], ["personne_nom","personne_prenom"])
			],
			null
		)

		voituresMetadata = new TableMetadata(
			"voitures",
			null,
			["marque","modele_id","couleur","personne_nom","personne_prenom"],
			["marque", "modele_id"],
			[
				personnes:	new ForeignKeySet(null, "personnes", ["nom","prenom"], ["personne_nom","personne_prenom"])
			],
			null
		)
		
		employeMetadata = new TableMetadata(
			"employes",
			null,
			["id","name","age"],
			["id"],
			null,
			null
		)
		
		joinTree = new JoinTree(
			"classes", ["personnes","personnes.enfants","personnes.voitures"],
			[
				classes: 	classesMetadata,
				personnes: 	personnesMetadata,
				enfants:	enfantsMetadata,
				voitures:	voituresMetadata,
				employes:	employeMetadata
			]
		)
	}

	@Test
	void testBuildJoinTree() {
		def joinTreeMap = joinTree.asMap()
		assert joinTreeMap.size() == 4
		assert joinTreeMap["classes"].resultsetStartIndex == 0
		assert joinTreeMap["classes"].resultsetEndIndex == 3
		assert joinTreeMap["classes"].parent == null
		assert joinTreeMap["classes"].children.size() == 1
		assert joinTreeMap["classes"].children[0] == joinTreeMap["classes.personnes"]
		assert joinTreeMap["classes.personnes"].resultsetStartIndex == 3
		assert joinTreeMap["classes.personnes"].resultsetEndIndex == 10
		assert joinTreeMap["classes.personnes"].parent == joinTreeMap["classes"]
		assert joinTreeMap["classes.personnes"].children.size() == 2
		assert joinTreeMap["classes.personnes"].children[0] == joinTreeMap["classes.personnes.enfants"]
		assert joinTreeMap["classes.personnes"].children[1] == joinTreeMap["classes.personnes.voitures"]
		assert joinTreeMap["classes.personnes.enfants"].resultsetStartIndex == 10
		assert joinTreeMap["classes.personnes.enfants"].resultsetEndIndex == 15
		assert joinTreeMap["classes.personnes.enfants"].parent == joinTreeMap["classes.personnes"]
		assert joinTreeMap["classes.personnes.enfants"].children.size() == 0
		assert joinTreeMap["classes.personnes.voitures"].resultsetStartIndex == 15
		assert joinTreeMap["classes.personnes.voitures"].resultsetEndIndex == 20
		assert joinTreeMap["classes.personnes.voitures"].parent == joinTreeMap["classes.personnes"]
		assert joinTreeMap["classes.personnes.voitures"].children.size() == 0
	}
	
	@Test
	void testSelectJoin() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(classesMetadata)
		selectQuery.addWhereClauses(id:[$ne:1,$gt:10])
		selectQuery.addJoins joinTree
		print selectQuery.build()
		assert selectQuery.build() == "SELECT * FROM classes "+
			"LEFT OUTER JOIN personnes ON personnes.classe_id = classes.id AND personnes.classe_floor = classes.floor " +
			"LEFT OUTER JOIN enfants ON enfants.personne_nom = personnes.nom AND enfants.personne_prenom = personnes.prenom "+
			"LEFT OUTER JOIN voitures ON voitures.personne_nom = personnes.nom AND voitures.personne_prenom = personnes.prenom "+
			"WHERE ( classes.id != :classes_id_0 AND classes.id > :classes_id_1 )"
	}
	
	@Test
	void testCriterionOr() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			id:		[ $ne:1, $gt:10],
			$or: 	[
				[
					name: "Damien",
					good:	true
				],
				[
					"employes.age": [$lt:10]
				]
			]
		])
		assert selectQuery.build() == "SELECT * FROM employes WHERE ( ( employes.id != :employes_id_0 AND employes.id > :employes_id_1 ) AND ( ( employes.name = :employes_name_0 AND employes.good = :employes_good_0 ) OR employes.age < :employes_age_0 ) )"
	}
	
	@Test
	void testOr(){
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([$or:[[first_name:"Carlos"],[first_name:"George"]]])
		print selectQuery.build()
		assert selectQuery.build() == "SELECT * FROM employes WHERE ( employes.first_name = :employes_first_name_0 OR employes.first_name = :employes_first_name_1 )"
	}
	
	@Test
	void testInCriterion () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$in:[30,31,32,33,34]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM employes WHERE employes.age IN ( :employes_age_0, :employes_age_1, :employes_age_2, :employes_age_3, :employes_age_4)"
	}
	
	@Test
	void testNotInCriterion () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$nin:[30,31,32,33,34]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM employes WHERE employes.age NOT IN ( :employes_age_0, :employes_age_1, :employes_age_2, :employes_age_3, :employes_age_4)"
	}
	
	@Test
	void testNotInCriterionModulo () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [
					$nin:[30,31,32,33,34],
					$mod:[10,0]
					]
				]
			]
		)
		assert selectQuery.build() == "SELECT * FROM employes WHERE ( employes.age NOT IN ( :employes_age_0, :employes_age_1, :employes_age_2, :employes_age_3, :employes_age_4) AND MOD(employes.age,:employes_age_5) = :employes_age_6 )"
	}
	
	@Test
	void testNotInCriterionModulo2 () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			$and: [
				["age" : [$nin:[30,31,32,33,34]]],
				["age" : [$mod:[10,0]]]
			]
		])
		assert selectQuery.build() == "SELECT * FROM employes WHERE ( employes.age NOT IN ( :employes_age_0, :employes_age_1, :employes_age_2, :employes_age_3, :employes_age_4) AND MOD(employes.age,:employes_age_5) = :employes_age_6 )"
	}
	
	@Test
	void testCriterionModulo () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$mod:[10,0]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM employes WHERE MOD(employes.age,:employes_age_0) = :employes_age_1"
	}
	
	@Test
	void testUpdateMethod() {
		QueryWithCriteria query = queryBuilder.buildUpdateQuery(personnesMetadata, [adresse:"Rue de la rue",age:3])
		query.addWhereClauses([nom:"Solimando",prenom:"Damien"])
		assert "UPDATE personnes SET network_id=:personnes_network_id_0, classe_floor=:personnes_classe_floor_0, adresse=:personnes_adresse_0, nom=:personnes_nom_1, prenom=:personnes_prenom_1, age=:personnes_age_0, classe_id=:personnes_classe_id_0 WHERE ( personnes.nom = :personnes_nom_0 AND personnes.prenom = :personnes_prenom_0 )" == query.build()
	}

    @Test
    void testUpdateMethodWith$Set() {
        QueryWithCriteria query = queryBuilder.buildUpdateQuery(personnesMetadata, [$set:[adresse:"Rue de la rue",age:3]])
        query.addWhereClauses([nom:"Solimando",prenom:"Damien"])
        assert "UPDATE personnes SET adresse=:personnes_adresse_0, age=:personnes_age_0 WHERE ( personnes.nom = :personnes_nom_0 AND personnes.prenom = :personnes_prenom_0 )" == query.build()
    }
	
	@Test
	void testDeleteMethod() {
		QueryWithCriteria query = queryBuilder.buildDeleteQuery(personnesMetadata)
		query.addWhereClauses(["nom":"Solimando","prenom":"Damien"])
		print query.build()
		assert "DELETE FROM personnes WHERE ( personnes.nom = :personnes_nom_0 AND personnes.prenom = :personnes_prenom_0 )" == query.build()
	}
	
	@Test
	void testInsertMethod() {
		Query query = queryBuilder.buildInsertQuery(personnesMetadata, ["id","address","age"])
		assert "INSERT INTO personnes (id,address,age) VALUES (:id,:address,:age)" == query.build()
	}
	
	@Test
	void testOrderBy() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: 1]
		assert "SELECT * FROM employes ORDER BY id ASC" == selectQuery.build()
	}
	
	@Test
	void testOrderBy2() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: -1]
		assert "SELECT * FROM employes ORDER BY id DESC" == selectQuery.build()
	}
	
	@Test
	void testOrderBy3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: -1, age: 1]
		assert "SELECT * FROM employes ORDER BY id DESC, age ASC" == selectQuery.build()
	}
	
	@Test
	void testLike () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			name: [$like: '%mien%']
		])
		assert selectQuery.build() == "SELECT * FROM employes WHERE employes.name LIKE :employes_name_0"
	}
	
	@Test
	void testPleinSqlWhere() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([name: [$where: "UCASE(employes.first_name) = 'GEOERGE' "],"age" : 1])
		assert selectQuery.build() == "SELECT * FROM employes WHERE ( ( UCASE(employes.first_name) = 'GEOERGE' ) AND employes.age = :employes_age_0 )"
	}
}
