package be.solidx.hot.test.data.jdbc.sql

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

public class TestCommonSqlQueryBuilder {

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
			"sc1",		// Schema
			["id", "floor", "name"], // Columns
			["id", "floor"],		// Primary Keys
			null,					// Foreign Keys
			[						// ExportedForeign Keys
				personnes: new ForeignKeySet("sc1","personnes", ["id", "floor"], ["classe_id", "classe_floor"])
			]
		)

		personnesMetadata = new TableMetadata(
			"personnes",
			"sc1",
			["nom","prenom","age","adresse","classe_id", "classe_floor","network_id"],
			["nom", "prenom"],
			[
				classes	:	new ForeignKeySet("sc1", "classes", ["id", "floor"], ["classe_id", "classe_floor"]),
				networks: 	new ForeignKeySet("sc1", "classes", ["id"], ["network_id"])
			],
			[
				enfants: 	new ForeignKeySet("sc1","enfants", ["nom", "prenom"], ["personne_nom","presonne_prenom"]),
				voitures:	new ForeignKeySet("sc1","voitures", ["nom", "prenom"], ["personne_nom","presonne_prenom"])
			]
		)

		enfantsMetadata = new TableMetadata(
			"enfants",
			"sc1",
			["nom", "prenom", "age","personne_nom","personne_prenom"],
			["nom", "prenom"],
			[
				personnes:	new ForeignKeySet("sc1", "personnes", ["nom","prenom"], ["personne_nom","personne_prenom"])
			],
			null
		)

		voituresMetadata = new TableMetadata(
			"voitures",
			"sc1",
			["marque","modele_id","couleur","personne_nom","personne_prenom"],
			["marque", "modele_id"],
			[
				personnes:	new ForeignKeySet("sc1", "personnes", ["nom","prenom"], ["personne_nom","personne_prenom"])
			],
			null
		)
		
		employeMetadata = new TableMetadata(
			"employes",
			"sc1",
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
		assert selectQuery.build() == "SELECT * FROM sc1.classes "+
			"LEFT OUTER JOIN sc1.personnes ON sc1.personnes.classe_id = sc1.classes.id AND sc1.personnes.classe_floor = sc1.classes.floor " +
			"LEFT OUTER JOIN sc1.enfants ON sc1.enfants.personne_nom = sc1.personnes.nom AND sc1.enfants.personne_prenom = sc1.personnes.prenom "+
			"LEFT OUTER JOIN sc1.voitures ON sc1.voitures.personne_nom = sc1.personnes.nom AND sc1.voitures.personne_prenom = sc1.personnes.prenom "+
			"WHERE ( sc1.classes.id != :sc1_classes_id_0 AND sc1.classes.id > :sc1_classes_id_1 )"
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
		print selectQuery.build()
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE ( ( sc1.employes.id != :sc1_employes_id_0 AND sc1.employes.id > :sc1_employes_id_1 ) AND ( ( sc1.employes.name = :sc1_employes_name_0 AND sc1.employes.good = :sc1_employes_good_0 ) OR sc1.employes.age < :sc1_employes_age_0 ) )"
	}
	
	
	@Test
	void testOr(){
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([$or:[[first_name:"Carlos"],[first_name:"George"]]])
		print selectQuery.build()
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE ( sc1.employes.first_name = :sc1_employes_first_name_0 OR sc1.employes.first_name = :sc1_employes_first_name_1 )"
	}
	
	@Test
	void testInCriterion () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$in:[30,31,32,33,34]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE sc1.employes.age IN ( :sc1_employes_age_0, :sc1_employes_age_1, :sc1_employes_age_2, :sc1_employes_age_3, :sc1_employes_age_4)"
	}
	
	@Test
	void testNotInCriterion () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$nin:[30,31,32,33,34]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE sc1.employes.age NOT IN ( :sc1_employes_age_0, :sc1_employes_age_1, :sc1_employes_age_2, :sc1_employes_age_3, :sc1_employes_age_4)"
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
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE ( sc1.employes.age NOT IN ( :sc1_employes_age_0, :sc1_employes_age_1, :sc1_employes_age_2, :sc1_employes_age_3, :sc1_employes_age_4) AND MOD(sc1.employes.age,:sc1_employes_age_5) = :sc1_employes_age_6 )"
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
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE ( sc1.employes.age NOT IN ( :sc1_employes_age_0, :sc1_employes_age_1, :sc1_employes_age_2, :sc1_employes_age_3, :sc1_employes_age_4) AND MOD(sc1.employes.age,:sc1_employes_age_5) = :sc1_employes_age_6 )"
	}
	
	@Test
	void testCriterionModulo () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
				["age" : [$mod:[10,0]]]
			]
		)
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE MOD(sc1.employes.age,:sc1_employes_age_0) = :sc1_employes_age_1"
	}
	
	@Test
	void testUpdateMethod() {
		QueryWithCriteria query = queryBuilder.buildUpdateQuery(personnesMetadata, [adresse:"Rue de la rue",age:3])
		query.addWhereClauses([nom:"Solimando",prenom:"Damien", adresse:"14 rue du perlonjour"])
		assert "UPDATE sc1.personnes SET sc1.personnes.adresse=:sc1_personnes_adresse_1, sc1.personnes.age=:sc1_personnes_age_0 WHERE ( sc1.personnes.nom = :sc1_personnes_nom_0 AND sc1.personnes.prenom = :sc1_personnes_prenom_0 AND sc1.personnes.adresse = :sc1_personnes_adresse_0 )" == query.build()
	}
	
	@Test
	void testDeleteMethod() {
		QueryWithCriteria query = queryBuilder.buildDeleteQuery(personnesMetadata)
		query.addWhereClauses(["nom":"Solimando","prenom":"Damien"])
		print query.build()
		assert "DELETE FROM sc1.personnes WHERE ( sc1.personnes.nom = :sc1_personnes_nom_0 AND sc1.personnes.prenom = :sc1_personnes_prenom_0 )" == query.build()
	}
	
	@Test
	void testInsertMethod() {
		Query query = queryBuilder.buildInsertQuery(personnesMetadata, ["id","address","age"])
		assert "INSERT INTO sc1.personnes (id,address,age) VALUES (:id,:address,:age)" == query.build()
	}
	
	@Test
	void testOrderBy() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: 1]
		assert "SELECT * FROM sc1.employes ORDER BY id ASC" == selectQuery.build()
	}
	
	@Test
	void testOrderBy2() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: -1]
		assert "SELECT * FROM sc1.employes ORDER BY id DESC" == selectQuery.build()
	}
	
	@Test
	void testOrderBy3() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.sortMap = [id: -1, age: 1]
		assert "SELECT * FROM sc1.employes ORDER BY id DESC, age ASC" == selectQuery.build()
	}
	
	@Test
	void testLike () {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([
			name: [$like: '%mien%']
		])
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE sc1.employes.name LIKE :sc1_employes_name_0"
	}
	
	@Test
	void testPleinSqlWhere() {
		SelectQuery selectQuery = queryBuilder.buildSelectQuery(employeMetadata)
		selectQuery.addWhereClauses([name: [$where: "UCASE(sc1.employes.first_name) = 'GEOERGE' "],"age" : 1])
		assert selectQuery.build() == "SELECT * FROM sc1.employes WHERE ( ( UCASE(sc1.employes.first_name) = 'GEOERGE' ) AND sc1.employes.age = :sc1_employes_age_0 )"
	}
}
