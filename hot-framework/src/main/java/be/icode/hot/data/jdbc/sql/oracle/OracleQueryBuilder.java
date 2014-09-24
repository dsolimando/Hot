package be.icode.hot.data.jdbc.sql.oracle;

import java.util.List;
import java.util.Map;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.Query;
import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.data.jdbc.sql.QueryWithCriteria;
import be.icode.hot.data.jdbc.sql.SelectQuery;
import be.icode.hot.data.jdbc.sql.impl.InsertQuery;

public class OracleQueryBuilder implements QueryBuilder {

	private static OracleQueryBuilder oracleQueryBuilder;
	
	private CriterionFactory criterionFactory = new OracleCriterionFactory();
	
	public static QueryBuilder getInstance () {
		if (oracleQueryBuilder == null) {
			oracleQueryBuilder = new OracleQueryBuilder();
		}
		return oracleQueryBuilder;
	}
	
	@Override
	public SelectQuery buildSelectQuery(TableMetadata tableMetadata) {
		return new OracleSelectQuery(tableMetadata, criterionFactory);
	}

	@Override
	public QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata) {
		return new OracleDeleteQuery(tableMetadata, criterionFactory);
	}

	@Override
	public Query buildInsertQuery(TableMetadata tableMetadata, List<String> insertParamaters) {
		return new InsertQuery(tableMetadata, insertParamaters);
	}

	@Override
	public QueryWithCriteria buildUpdateQuery(TableMetadata tableMetadata, Map<String,Object> updateParameters) {
		return new OracleUpdateQuery(tableMetadata, updateParameters, criterionFactory);
	}

}
