package be.solidx.hot.data.jdbc.sql;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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

import be.solidx.hot.data.jdbc.sql.db2.DB2QueryBuilder;
import be.solidx.hot.data.jdbc.sql.mysql.MysqlQueryBuilder;
import be.solidx.hot.data.jdbc.sql.oracle.OracleQueryBuilder;
import be.solidx.hot.data.jdbc.sql.postgres.PgsqlQueryBuilder;

public class QueryBuilderFactory {

	public static QueryBuilder buildQueryBuilder (DBEngine dbengine) {
		
		switch (dbengine) {
		case MYSQL:
		case H2:
		case HSQLDB:
			return MysqlQueryBuilder.getInstance();
		case ORACLE:
			return OracleQueryBuilder.getInstance();
		case PGSQL:
			return PgsqlQueryBuilder.getInstance();
		case DB2:
			return DB2QueryBuilder.getInstance();
		default:
			throw new RuntimeException("Not Implemented");
		}
	}
	
	public enum DBEngine {
		MYSQL, PGSQL, ORACLE, DB2, INFORMIX, HSQLDB, H2
	}
}
