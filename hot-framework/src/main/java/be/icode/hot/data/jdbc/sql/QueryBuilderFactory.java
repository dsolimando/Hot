package be.icode.hot.data.jdbc.sql;

import be.icode.hot.data.jdbc.sql.db2.DB2QueryBuilder;
import be.icode.hot.data.jdbc.sql.mysql.MysqlQueryBuilder;
import be.icode.hot.data.jdbc.sql.oracle.OracleQueryBuilder;
import be.icode.hot.data.jdbc.sql.postgres.PgsqlQueryBuilder;

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
