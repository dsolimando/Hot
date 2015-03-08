package be.solidx.hot.data.jdbc.sql.criterion;

import be.solidx.hot.data.criterion.Criterion;

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
