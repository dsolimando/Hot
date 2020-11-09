package be.solidx.hot.test.data.jdbc

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
 
import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext

import be.solidx.hot.data.DB;
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory
import be.solidx.hot.data.jdbc.sql.QueryBuilderFactory.DBEngine;

class PgDBAccess {
	static def main (args) {
		def appContext = new ClassPathXmlApplicationContext("be/solidx/hot/test/data/jdbc/pgDS.xml")
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
