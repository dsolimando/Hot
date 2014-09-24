package be.icode.hot.data.jdbc.sql.postgres;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.impl.AbstractDeleteQuery;
import be.icode.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class PgsqlDeleteQuery extends AbstractDeleteQuery {

	public PgsqlDeleteQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}

}
