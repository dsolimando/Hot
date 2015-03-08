package be.solidx.hot.data.jdbc.sql.db2;

import java.util.List;
import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.Query;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.data.jdbc.sql.impl.InsertQuery;

public class DB2QueryBuilder implements QueryBuilder {

	private static DB2QueryBuilder db2QueryBuilder;
	
	private CriterionFactory criterionFactory = new DB2CriterionFactory();
	
	private DB2QueryBuilder() {}
	
	public static QueryBuilder getInstance() {
		if (db2QueryBuilder == null) db2QueryBuilder = new DB2QueryBuilder();
		return db2QueryBuilder;
	}
	
	@Override
	public SelectQuery buildSelectQuery(TableMetadata tableMetadata) {
		return new DB2SelectQuery(tableMetadata, criterionFactory);
	}

	@Override
	public QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata) {
		return new DB2DeleteQuery(tableMetadata, criterionFactory);
	}

	@Override
	public Query buildInsertQuery(TableMetadata tableMetadata, List<String> insertParamaters) {
		return new InsertQuery(tableMetadata, insertParamaters);
	}

	@Override
	public QueryWithCriteria buildUpdateQuery(TableMetadata tableMetadata, Map<String, Object> updateParameters) {
		return new DB2UptadeQuery(tableMetadata, updateParameters, criterionFactory);
	}
}
