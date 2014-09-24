package be.icode.hot.test.data.jdbc

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext

import be.icode.hot.data.DB;
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory
import be.icode.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

class PgDBAccess {
	static def main (args) {
		def appContext = new ClassPathXmlApplicationContext("be/icode/hot/test/data/jdbc/pgDS.xml")
		def test = new TestCollectionApi()

		test.db = appContext.getBean(DB.class)
		test.testFindAll()
		test.testFindJoinWhere1()
		test.testFindWhere1()
		test.testFindWhere2()
		test.testFindWhere3()
		test.testFindWhere4()
		test.testFindWhere5()
		test.testFindWhereLike()
		test.testIn()
		test.testJoinIn()
		test.testFindWhereModulo()
		test.testFindWhereModuloIn()
		test.testJoinInModulo()
		test.testJoinInModuloLimit()
		test.testJoinInModuloLimitOffset()
	}
}
