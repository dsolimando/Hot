package be.icode.hot.data.jdbc.sql.db2;

import java.util.Map;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.impl.AbstractUpdateQuery;
import be.icode.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class DB2UptadeQuery extends AbstractUpdateQuery {

	public DB2UptadeQuery(TableMetadata tableMetadata, Map<String, Object> updateParameters, CriterionFactory criterionFactory) {
		super(tableMetadata, updateParameters, criterionFactory);
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
