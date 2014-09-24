package be.icode.hot.data.jdbc.sql.mysql;

import java.util.List;
import java.util.Map;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.Query;
import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.QueryWithCriteria;
import be.icode.hot.data.jdbc.sql.SelectQuery;
import be.icode.hot.data.jdbc.sql.impl.InsertQuery;

public class MysqlQueryBuilder implements QueryBuilder {
	
	private MysqlQueryBuilder () {}
	
	private CriterionFactory criterionFactory = new MysqlCriterionFactory();
	
	private static MysqlQueryBuilder mysqlQueryBuilder;
	
	public static QueryBuilder getInstance () {
		if (mysqlQueryBuilder == null) {
			mysqlQueryBuilder = new MysqlQueryBuilder();
		}
		return mysqlQueryBuilder;
	}

	@Override
	public SelectQuery buildSelectQuery (TableMetadata tableMetadata) {
		return new MysqlSelectQuery(tableMetadata, criterionFactory);
	}
	
	@Override
	public Query buildInsertQuery (TableMetadata tableMetadata, List<String> insertParamaters) {
		return new InsertQuery(tableMetadata, insertParamaters);
	}
	
	@Override
	public QueryWithCriteria buildUpdateQuery (TableMetadata tableMetadata, Map<String,Object> updateParameters) {
		return new MysqlUpdateQuery(tableMetadata, updateParameters, criterionFactory);
	}

	@Override
	public QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata) {
		return new MysqlDeleteQuery(tableMetadata, criterionFactory);
	}
}
