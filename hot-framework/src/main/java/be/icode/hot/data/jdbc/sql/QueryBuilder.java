package be.icode.hot.data.jdbc.sql;

import java.util.List;
import java.util.Map;

import be.icode.hot.data.jdbc.TableMetadata;

public interface QueryBuilder {

	SelectQuery buildSelectQuery(TableMetadata tableMetadata);
	
	QueryWithCriteria buildDeleteQuery(TableMetadata tableMetadata);

	Query buildInsertQuery(TableMetadata tableMetadata, List<String> insertParamaters);

	QueryWithCriteria buildUpdateQuery (TableMetadata tableMetadata, Map<String,Object> updateParameters);
}