package be.icode.hot.data.jdbc.sql.criterion;

import be.icode.hot.data.criterion.Criterion;

public class SqlCriterion implements Criterion {

	String expression;

	public SqlCriterion(String expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "( "+expression+")";
	}
}
