package be.solidx.hot.data.jdbc.sql.mysql;

import java.util.Map;

import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.impl.AbstractUpdateQuery;

public class MysqlUpdateQuery extends AbstractUpdateQuery {
	
	public MysqlUpdateQuery(TableMetadata tableMetadata, Map<String, Object> updateParameters, CriterionFactory criterionFactory) {
		super(tableMetadata, updateParameters, criterionFactory);
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
