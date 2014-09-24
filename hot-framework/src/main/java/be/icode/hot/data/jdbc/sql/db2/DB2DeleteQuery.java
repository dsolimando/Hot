package be.icode.hot.data.jdbc.sql.db2;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class DB2DeleteQuery extends be.icode.hot.data.jdbc.sql.impl.AbstractDeleteQuery {

	public DB2DeleteQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
}
