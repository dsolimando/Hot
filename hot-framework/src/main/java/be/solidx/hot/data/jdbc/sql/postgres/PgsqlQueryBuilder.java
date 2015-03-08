package be.solidx.hot.data.jdbc.sql.postgres;

import java.util.List;
import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.Query;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria;
import be.solidx.hot.data.jdbc.sql.SelectQuery;
import be.solidx.hot.data.jdbc.sql.impl.InsertQuery;

public class PgsqlQueryBuilder implements QueryBuilder {
	
	private static PgsqlQueryBuilder pgsqlQueryBuilder;
	
	private CriterionFactory criterionFactory = new PgsqlCriterionFactory();
	
	private PgsqlQueryBuilder() {}
	
	public static PgsqlQueryBuilder getInstance() {
		if (pgsqlQueryBuilder == null) {
			pgsqlQueryBuilder = new PgsqlQueryBuilder();
		}
		return pgsqlQueryBuilder;
	}

	@Override
	public SelectQuery buildSelectQuery(TableMetadata tableMetadata) {
		return new PgsqlSelectQuery(tableMetadata, criterionFactory);
	}

	@Override
	public QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata) {
		return new PgsqlDeleteQuery(tableMetadata, criterionFactory);
	}

	@Override
	public Query buildInsertQuery(TableMetadata tableMetadata, List<String> insertParamaters) {
		return new InsertQuery(tableMetadata, insertParamaters);
	}

	@Override
	public QueryWithCriteria buildUpdateQuery(TableMetadata tableMetadata, Map<String,Object> updateParameters) {
		return new PgsqlUpdateQuery(tableMetadata, updateParameters, criterionFactory);
	}

}
