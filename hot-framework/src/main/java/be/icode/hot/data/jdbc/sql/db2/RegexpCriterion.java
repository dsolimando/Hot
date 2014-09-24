package be.icode.hot.data.jdbc.sql.db2;

import be.icode.hot.data.criterion.Operator;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;

public class RegexpCriterion extends CriterionImpl {

	public RegexpCriterion(String parameterName, int index) {
		super(Operator.$regex, parameterName, index);
	}

	@Override
	public String toString() {
		throw new RuntimeException("Not Implemented");
	}
}
